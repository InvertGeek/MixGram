package com.donut.mixgram.activity.chat_page.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.donut.mixgram.appScope
import com.donut.mixgram.component.routes.settings.utils.setUserProfile
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.ignoreError
import com.donut.mixgram.util.mixfile.selectFilesUpload
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


var forwardMsg by mutableStateOf(listOf<String>())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessage(group: ChatGroup) {
    HorizontalDivider()

    var input by remember { mutableStateOf(TextFieldValue("")) }

    var sending by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    fun sendMsg(message: List<String>, onError: () -> Unit = {}) {
        if (message.isEmpty()) {
            return
        }
        scope.launch(Dispatchers.IO) {
            sending = true
            errorDialog(
                "发送失败",
                onError = {
                    withContext(Dispatchers.Main) {
                        onError()
                    }
                }) {
                group.sendMessage(message)
                showToast("发送成功")
            }
            sending = false
            ignoreError {
                group.trimCommits(group.commitsLimit)
            }
        }
    }

    // 输入区
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (forwardMsg.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton({
                    forwardMsg = listOf()
                }) {
                    Text("取消转发")
                }
                Button(
                    enabled = !sending,
                    onClick = {
                        val msgToSend = forwardMsg
                        forwardMsg = listOf()
                        sendMsg(msgToSend) {
                            forwardMsg = msgToSend
                        }
                    },
                ) {
                    Text("发送转发信息")
                }
            }
            return
        }

        TextField(
            value = input,
            modifier = Modifier.weight(0.9f),
            onValueChange = { input = it },
            maxLines = 3,
            placeholder = { Text("输入消息...") },
            shape = RoundedCornerShape(50)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            enabled = !sending,
            onClick = {
                if (CHAT_USER_NAME.isBlank()) {
                    setUserProfile()
                    return@Button
                }
                val text = input.text
                if (text.isEmpty()) {
                    appScope.launch(Dispatchers.Main) {
                        val files = selectFilesUpload()
                        sendMsg(files.map { it.toString() })
                    }
                    return@Button
                }
                input = TextFieldValue("")
                sendMsg(listOf(text)) {
                    input = TextFieldValue(text)
                }
            },
            shape = RoundedCornerShape(20),
            elevation = ButtonDefaults.elevatedButtonElevation()
        ) {
            if (sending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                return@Button
            }
            Text(if (input.text.isEmpty()) "文件" else "发送")
        }
    }
}