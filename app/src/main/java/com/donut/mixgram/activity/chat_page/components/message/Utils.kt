package com.donut.mixgram.activity.chat_page.components.message

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatSmartTime(date: Date): String {
    val now = Date()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)

    return if (dateFormatter.format(date) == dateFormatter.format(now)) {
        // 同一天，只显示时分秒
        timeFormatter.format(date)
    } else {
        // 不同天，显示完整日期时间
        "${dateFormatter.format(date)} ${timeFormatter.format(date)}"
    }
}
