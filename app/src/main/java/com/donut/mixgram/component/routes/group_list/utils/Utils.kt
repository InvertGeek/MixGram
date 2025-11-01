package com.donut.mixgram.component.routes.group_list.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.InfoText
import com.donut.mixgram.util.objects.ChatGroup


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