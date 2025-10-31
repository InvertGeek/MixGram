package com.donut.mixgram.activity.chat_page.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.ignoreError
import com.donut.mixgram.util.objects.ChatGroup
import com.donut.mixgram.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMessage(group: ChatGroup) {

    var input by remember { mutableStateOf(TextFieldValue("")) }
    // 输入区
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 0.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        TextField(
            value = input,
            modifier = Modifier.weight(0.9f),
            onValueChange = { input = it },
            maxLines = 3,
            placeholder = { Text("输入消息...") },
            shape = RoundedCornerShape(50)
        )

        var sending by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()


        Button(
            enabled = input.text.isNotEmpty(),
            onClick = {
                val text = input.text
                input = TextFieldValue("")
                scope.launch(Dispatchers.IO) {
                    sending = true
                    errorDialog(
                        "发送失败",
                        onError = {
                            withContext(Dispatchers.Main) {
                                input = TextFieldValue(text)
                            }
                        }) {
                        group.sendMessage(listOf(text))
                        showToast("发送成功")
                    }
                    sending = false
                    ignoreError {
                        group.trimCommits(group.commitsLimit)
                    }
                }

            },
            shape = RoundedCornerShape(50)
        ) {
            if (sending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                return@Button
            }
            Text("发送")
        }
    }
}