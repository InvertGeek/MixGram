package com.donut.mixgram.component.routes.group_list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.addChatGroup
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showToast

fun addGroup() {
    MixDialogBuilder("添加群组").apply {

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

        setPositiveButton("添加群组") {
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
            showToast("添加成功")
            closeDialog()
        }

        show()
    }
}