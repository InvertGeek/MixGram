package com.donut.mixgram.activity.chat_page.components.message

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


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