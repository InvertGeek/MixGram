package com.donut.mixgram.component.routes.group_list.utils

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.addChatGroup
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showTipDialog
import com.donut.mixgram.util.showToast

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