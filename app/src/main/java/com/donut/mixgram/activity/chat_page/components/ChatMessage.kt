package com.donut.mixgram.activity.chat_page.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.copyToClipboard
import com.donut.mixgram.util.objects.UserMessage
import com.donut.mixgram.util.showToast
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

@Composable
fun HighlightAndClickableUrls(text: String, color: Color) {
    val context = LocalContext.current
    val urIPattern =
        "([a-zA-Z0-9]+)://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}(\\.)?[a-zA-Z0-9()]{1,6}(\\b)?([-a-zA-Z0-9()@:%_+.~#?&/=]*)".toRegex()


    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        urIPattern.findAll(text).forEach { matchResult ->
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            // 添加之前的普通文本
            withStyle(style = SpanStyle(fontSize = 16.sp, color = color)) {
                append(text.substring(lastIndex, startIndex))
            }

            // 添加高亮的 URL 并且添加点击注释
            pushStringAnnotation(tag = "URI", annotation = matchResult.value)
            withStyle(
                style = SpanStyle(
                    color = color,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(matchResult.value)
            }
            pop()

            lastIndex = endIndex
        }
        // 添加剩余的普通文本
        withStyle(style = SpanStyle(fontSize = 16.sp, color = color)) {
            append(text.substring(lastIndex))
        }
    }

    ClickableText(
        style = TextStyle(),
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URI", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    MixDialogBuilder("打开URI?").apply {
                        setContent {
                            Text(text = annotation.item, color = colorScheme.primary)
                        }
                        setNegativeButton("复制") {
                            annotation.item.copyToClipboard()
                            closeDialog()
                        }
                        setPositiveButton("确定") {
                            closeDialog()
                            try {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                showToast("没有可以处理此URI的应用")
                            }
                        }
                        show()
                    }
                }
        }
    )
}

@Composable
fun MessageRow(message: UserMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        @Composable
        fun TimeTip() {
            val timeContent = formatSmartTime(Date(message.date))
            val content = if (message.valid) timeContent else "${timeContent} Git原信息(解密失败)"
            val color =
                if (message.valid) colorScheme.primary.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)

            Text(
                text = content,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        @Composable
        fun MsgContent() {
            message.message.forEach {
                Bubble(text = it)
            }
        }

        @Composable
        fun UserName() {
            Text(
                text = message.userName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
        if (!message.isMe) {
            // 对方：头像 + 内容（左侧）
            Avatar(name = message.userName)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.widthIn(max = 260.dp)) {
                // 用户名
                UserName()
                // 气泡
                MsgContent()
                // 时间
                TimeTip()
            }
        } else {
            // 我：内容 + 头像  (右侧)
            Column(
                modifier = Modifier.widthIn(max = 260.dp),
                horizontalAlignment = Alignment.End
            ) {
                UserName()
                MsgContent()
                TimeTip()
            }
            Spacer(modifier = Modifier.width(8.dp))
            Avatar(name = message.userName)
        }
    }
}

@Composable
fun Avatar(name: String, size: Int = 40) {
    // 简易头像占位：用首字母显示圆形背景。真实项目建议用 Coil/Glide 加载头像 url。
    val initials = name.trim().takeIf { it.isNotEmpty() }?.first()?.uppercaseChar() ?: '?'
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFFB5BFCA))
            .clickable { /* 可点击查看用户信息 */ },
        contentAlignment = Alignment.Center
    ) {
        Text(initials.toString(), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Bubble(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = colorScheme.primary.copy(0.1f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        SelectionContainer {
            HighlightAndClickableUrls(text = text, color = colorScheme.primary)
        }

    }
}