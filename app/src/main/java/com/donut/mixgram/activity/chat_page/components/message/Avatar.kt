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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixgram.activity.chat_page.components.message.files.ErrorMessage
import com.donut.mixgram.genImageLoader
import com.donut.mixgram.util.mixfile.downloadUrl

@Composable
fun Avatar(name: String, shareInfo: MixShareInfo? = null, size: Int = 40, onClick: () -> Unit) {

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
        if (shareInfo != null && shareInfo.fileSize < 1024 * 1024) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(shareInfo.downloadUrl)
                    .crossfade(true)
                    .build(),
                error = {
                    ErrorMessage(msg = "图片加载失败")
                },
                contentScale = ContentScale.FillBounds,
                imageLoader = genImageLoader(
                    LocalContext.current
                ),
                contentDescription = name,
            )
            return
        }
        Text(initials.toString(), color = Color.White, fontWeight = FontWeight.Bold)
    }
}
