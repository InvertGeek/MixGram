package com.donut.mixgram.component.routes.group_list

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixgram.activity.chat_page.ChatPage
import com.donut.mixgram.app
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.startActivity

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun GroupCard(group: ChatGroup) {
    LaunchedEffect(CHAT_GROUPS) {

    }
    val color = Color(107, 218, 246, 0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .combinedClickable(
                onLongClick = {
                    editGroup(group)
                }
            ) {
                startActivity(Intent(app, ChatPage::class.java).apply {
                    putExtra("group", group.name)
                })
            }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = group.name.trim(),
                    color = colorScheme.primary,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = group.repoUrl,
                    color = Color.Gray,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    fontSize = 14.sp
                )

            }
        }
    }
}