package com.donut.mixgram.component.routes.group_list.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixgram.appScope
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.component.routes.settings.utils.SettingButton
import com.donut.mixgram.component.routes.settings.utils.setStringValue
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.InfoText
import com.donut.mixgram.util.editChatGroup
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.formatTime
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showConfirmDialog
import com.donut.mixgram.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

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
                SettingButton(text = "SSH私钥: ") {
                    setStringValue("设置SSH私钥", group.sshKey, "SSH私钥", {
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
