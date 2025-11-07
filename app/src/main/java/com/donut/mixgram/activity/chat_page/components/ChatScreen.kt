package com.donut.mixgram.activity.chat_page.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixgram.activity.chat_page.components.message.MessageRow
import com.donut.mixgram.util.AsyncEffect
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.objects.UserMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(group: ChatGroup) {
    var messages: List<UserMessage> by remember { mutableStateOf(listOf()) }
    var loaded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    AsyncEffect {
        errorDialog("拉取消息失败") {
            suspend fun fetchMessages() {
                try {
                    val fetchedMessages = group.fetchMessages() ?: listOf()
                    withContext(Dispatchers.Main) {
                        messages = fetchedMessages
                        loaded = true
                    }
                } catch (e: Throwable) {
                    if (messages.isEmpty()) {
                        throw e
                    }
                }
            }
            while (true) {
                fetchMessages()
                delay(500)
            }
        }
    }

    AnimatedVisibility(
        !loaded,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("消息拉取中", color = colorScheme.primary)
            }
        }
    }


    AnimatedVisibility(
        loaded,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(0.dp, 1000.dp),
                state = listState,
                verticalArrangement = Arrangement.Top,
                reverseLayout = true
            ) {
                item {
                    Text(
                        "共 ${messages.size} 条消息",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center,
                        color = Color.Gray.copy(0.8f)
                    )
                }
                items(messages.size) {
                    MessageRow(messages[it])
                }


            }
        }
    }


}

