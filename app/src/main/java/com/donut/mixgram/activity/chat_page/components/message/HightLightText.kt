package com.donut.mixgram.activity.chat_page.components.message

import android.content.Intent
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.copyToClipboard
import com.donut.mixgram.util.showToast

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

    val focusManager = LocalFocusManager.current

    ClickableText(
        style = TextStyle(),
        text = annotatedString,
        onClick = { offset ->
            focusManager.clearFocus()
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