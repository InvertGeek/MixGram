package com.donut.mixgram.component.routes.settings.utils

import com.donut.mixfile.server.core.utils.parseJsonObject
import com.donut.mixfile.server.core.utils.toJsonString
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.copyToClipboard
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.readClipBoardText
import com.donut.mixgram.util.showConfirmDialog
import com.donut.mixgram.util.showToast

fun manageGroupList() {
    MixDialogBuilder(
        "群组数据导入/导出",
        subtitle = "可将群组列表导出至剪贴板,或从剪贴板导入"
    ).apply {
        setPositiveButton("导出群组") {
            val data = CHAT_GROUPS.toJsonString()
            data.copyToClipboard()
        }
        setNegativeButton("导入群组") {
            try {
                val data = readClipBoardText().parseJsonObject<List<ChatGroup>>()
                showConfirmDialog("确定导入?") {
                    val size = CHAT_GROUPS.size
                    CHAT_GROUPS += data
                    CHAT_GROUPS = CHAT_GROUPS.distinctBy { it.name }
                    showToast("导入了: ${CHAT_GROUPS.size - size} 条数据")
                }
            } catch (_: Exception) {
                showToast("解析群组失败")
            }
        }
        show()
    }
}