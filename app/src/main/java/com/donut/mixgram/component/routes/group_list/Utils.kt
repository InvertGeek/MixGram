package com.donut.mixgram.component.routes.group_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixgram.appScope
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.component.routes.settings.SettingButton
import com.donut.mixgram.component.routes.settings.setStringValue
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.InfoText
import com.donut.mixgram.util.addChatGroup
import com.donut.mixgram.util.editChatGroup
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.formatTime
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showConfirmDialog
import com.donut.mixgram.util.showTipDialog
import com.donut.mixgram.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

fun importGroup() {
    var text by mutableStateOf("")
    MixDialogBuilder("导入群组").apply {
        setContent {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it.trim()
                },
                label = {
                    Text("输入分享码")
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5
            )
        }
        setDefaultNegative("取消")
        setPositiveButton("确定导入") {
            val group = ChatGroup.parseShareCode(text)
            if (group == null) {
                showToast("解析分享码失败")
                return@setPositiveButton
            }
            if (CHAT_GROUPS.any { it.name.contentEquals(group.name) }) {
                showTipDialog("相同群名称已经存在", "群名称: ${group.name}")
                return@setPositiveButton
            }
            addChatGroup(group)
            closeDialog()
            showTipDialog("导入成功", "群名称: ${group.name}")
        }
        show()
    }
}

fun createOrAddGroup() {
    MixDialogBuilder("选择操作").apply {
        setPositiveButton("导入群组") {
            importGroup()
            closeDialog()
        }
        setNegativeButton("创建群组") {
            createGroup()
            closeDialog()
        }
        show()
    }
}


fun createGroup() {
    MixDialogBuilder("创建群组").apply {

        var groupName by mutableStateOf("")

        var sshKey by mutableStateOf("")

        var repoUrl by mutableStateOf("")

        setContent {
            OutlinedTextField(
                value = groupName,
                onValueChange = {
                    groupName = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "群组名称")
                },
                maxLines = 2,
            )
            OutlinedTextField(
                value = repoUrl,
                onValueChange = {
                    repoUrl = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "仓库地址")
                },
                maxLines = 3,
            )
            OutlinedTextField(
                value = sshKey,
                onValueChange = {
                    sshKey = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "SSH私钥")
                },
                maxLines = 5,
            )
        }

        setPositiveButton("创建群组") {
            if (repoUrl.isEmpty()) {
                showToast("仓库地址不能为空")
                return@setPositiveButton
            }
            if (groupName.isEmpty()) {
                showToast("群组名称不能为空")
                return@setPositiveButton
            }
            if (sshKey.isEmpty()) {
                showToast("SSH私钥不能为空")
                return@setPositiveButton
            }
            val group = ChatGroup(
                repoUrl = repoUrl.trim(),
                name = groupName.trim(),
                sshKey = sshKey.trim()
            )
            addChatGroup(group)
            showToast("创建成功")
            closeDialog()
        }

        show()
    }
}

fun editGroup(group: ChatGroup) {
    MixDialogBuilder("编辑群组: ${group.name}").apply {
        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("仓库地址和秘钥不支持修改,可重新创建群组生成新的秘钥")
                InfoText("仓库地址: ", group.repoUrl)
                InfoText("秘钥: ", group.aesKey)
                InfoText("添加日期: ", formatTime(Date(group.date)))
                SettingButton(text = "群组名称(本地): ${group.name}") {
                    setStringValue("设置群组名称", group.name, "群组名称", {
                        val newGroup = group.copy(name = it.trim().take(20))
                        editChatGroup(group, newGroup)
                        showToast("修改成功")
                        closeDialog()
                        editGroup(newGroup)
                    })
                }
                SettingButton(text = "群组分享码: ", buttonText = "查看") {
                    shareGroup(group)
                }
                SettingButton("清空消息: ", buttonText = "清空") {
                    showConfirmDialog("确认清空群组消息?") {
                        appScope.launch(Dispatchers.IO) {
                            errorDialog("清空消息失败") {
                                group.sendMessage(listOf("消息已清空"))
                                group.trimCommits(1)
                                showToast("消息已清空")
                                closeDialog()
                            }
                        }
                    }
                }
                SettingButton(text = "SSH密钥: ") {
                    setStringValue("设置SSH秘钥", group.sshKey, "SSH秘钥", {
                        val newGroup = group.copy(sshKey = it.trim())
                        editChatGroup(group, newGroup)
                        showToast("修改成功")
                        closeDialog()
                        editGroup(newGroup)
                    }, 10)
                }
                SettingButton(text = "最大消息限制: ${group.commitsLimit}") {
                    setStringValue(
                        "设置最大消息限制",
                        group.commitsLimit.toString(),
                        "最大消息限制(10-10000)",
                        {
                            val limit = it.trim().toIntOrNull()
                            if (limit == null) {
                                showToast("请输入数字")
                                return@setStringValue
                            }
                            if (limit > 10000) {
                                showToast("不能超过10000")
                                return@setStringValue
                            }
                            if (limit < 10) {
                                showToast("不能小于10")
                                return@setStringValue
                            }
                            val newGroup = group.copy(commitsLimit = limit)
                            editChatGroup(group, newGroup)
                            showToast("修改成功")
                            closeDialog()
                            editGroup(newGroup)
                        },
                        10,
                        subTitle = "限制达到后发送消息时会自动删除超出信息",
                    )
                }
            }
        }
        setPositiveButton("关闭") {
            closeDialog()
        }
        setNegativeButton("删除群组") {
            showConfirmDialog("删除群组?") {
                CHAT_GROUPS -= group
                closeDialog()
                showToast("删除成功")
            }
        }
        show()
    }
}

fun shareGroup(group: ChatGroup) {
    MixDialogBuilder("分享群组").apply {
        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                InfoText("分享码(点击复制):", group.getShareCode())
            }
        }
        setDefaultNegative("关闭")
        show()
    }
}