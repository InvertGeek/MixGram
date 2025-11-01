package com.donut.mixgram.activity.chat_page.components.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import com.donut.mixgram.activity.chat_page.components.message.files.FileRow
import com.donut.mixgram.appScope
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.InfoText
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.formatTime
import com.donut.mixgram.util.objects.UserMessage
import com.donut.mixgram.util.showConfirmDialog
import com.donut.mixgram.util.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date


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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val files = mutableListOf<MixShareInfo>()
                message.message.forEach {
                    val shareInfo = resolveMixShareInfo(it)
                    if (shareInfo != null) {
                        files += shareInfo
                        return@forEach
                    }
                    Bubble(text = it)
                }
                if (files.isNotEmpty()) {
                    FileRow(files)
                }
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

        fun deleteMsg(callback: () -> Unit) {
            showConfirmDialog("确认删除?") {
                callback()
                appScope.launch(Dispatchers.IO) {
                    errorDialog("删除失败") {
                        message.delete()
                        showToast("删除成功")
                    }
                }
            }
        }

        fun handleClick() {
            MixDialogBuilder("查看信息").apply {
                setContent {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val commit = message.commitMessage

                        InfoText(
                            "Commit日期: ",
                            formatTime(Date(commit?.date ?: System.currentTimeMillis()))
                        )

                        InfoText("Commit作者: ", commit?.author ?: "")

                        InfoText("Commit邮箱: ", commit?.email ?: "")

                        InfoText("解密后数据: ", message.decryptedMsg)

                        InfoText("Commit原文: ", commit?.message ?: "")
                    }

                }
                setNegativeButton("删除信息") {
                    deleteMsg {
                        closeDialog()
                    }
                }
                show()
            }
        }
        if (!message.isMe) {
            // 对方：头像 + 内容（左侧）
            Avatar(name = message.userName) {
                handleClick()
            }
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
            Avatar(name = message.userName) {
                handleClick()
            }
        }
    }
}

