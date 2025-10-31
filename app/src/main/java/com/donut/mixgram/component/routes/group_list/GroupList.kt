package com.donut.mixgram.component.routes.group_list

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixgram.activity.chat_page.ChatPage
import com.donut.mixgram.app
import com.donut.mixgram.component.nav.MixNavPage
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showConfirmDialog
import com.donut.mixgram.util.startActivity

val GroupList = MixNavPage(
    gap = 10.dp,
    horizontalAlignment = Alignment.CenterHorizontally,
    floatingButton = {
        FloatingActionButton(onClick = {
            addGroup()
        }, modifier = Modifier.padding(10.dp, 50.dp)) {
            Icon(Icons.Filled.Add, "Add Group")
        }
    }
) {
    var text by remember {
        mutableStateOf("")
    }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
        },
        modifier = Modifier.fillMaxWidth(), label = {
            Text(text = "输入群组名称")
        },
        maxLines = 3,
        trailingIcon = {
            if (text.isNotEmpty()) {
                Icon(
                    Icons.Outlined.Close,
                    tint = colorScheme.primary,
                    contentDescription = "clear",

                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        text = ""
                    })
            }
        }
    )
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(0.dp, 1000.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(CHAT_GROUPS.size) { index ->
                val group = CHAT_GROUPS[index]
                if (index > 0) {
                    HorizontalDivider()
                }
                GroupCard(group)

            }
        }
    }

}

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
                    showConfirmDialog("删除群组?") {
                        CHAT_GROUPS -= group
                    }
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