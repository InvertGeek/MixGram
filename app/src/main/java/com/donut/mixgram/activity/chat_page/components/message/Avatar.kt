package com.donut.mixgram.activity.chat_page.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Avatar(name: String, size: Int = 40, onClick: () -> Unit) {
    // 简易头像占位：用首字母显示圆形背景。真实项目建议用 Coil/Glide 加载头像 url。
    val initials = name.trim().takeIf { it.isNotEmpty() }?.first()?.uppercaseChar() ?: '?'
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFFB5BFCA))
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(initials.toString(), color = Color.White, fontWeight = FontWeight.Bold)
    }
}
