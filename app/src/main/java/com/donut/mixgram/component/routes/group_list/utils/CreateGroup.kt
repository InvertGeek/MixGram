package com.donut.mixgram.component.routes.group_list.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.InfoText
import com.donut.mixgram.util.addChatGroup
import com.donut.mixgram.util.generateKeyPair
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showToast

fun createGroup() {
    MixDialogBuilder("创建群组").apply {

        var groupName by mutableStateOf("")

        var sshKey by mutableStateOf("")

        var repoUrl by mutableStateOf("")

        setContent {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
                        Text(text = "仓库地址(SSH格式)")
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
        }

        setNegativeButton("生成密钥对") {
            generateSSHKeyPair()
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

fun generateSSHKeyPair() {

    val keyPair = generateKeyPair("mixgram")

    MixDialogBuilder("生成结果", autoClose = false).apply {
        setContent {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoText("公钥(点击复制): ", keyPair.second)
                InfoText("私钥(点击复制): ", keyPair.first)
            }
        }
        setDefaultNegative("关闭")
        show()
    }
}