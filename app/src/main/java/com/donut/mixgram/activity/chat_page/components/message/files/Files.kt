package com.donut.mixgram.activity.chat_page.components.message.files

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.utils.parseFileMimeType
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.AsyncEffect
import com.donut.mixgram.util.InfoText
import com.donut.mixgram.util.copyToClipboard
import com.donut.mixgram.util.formatFileSize
import com.donut.mixgram.util.mixfile.downloadUrl
import com.donut.mixgram.util.mixfile.saveFileToStorage
import com.donut.mixgram.util.objects.ProgressContent
import com.donut.mixgram.util.showToast
import com.donut.mixgram.util.startActivity

fun showFileWindow(file: MixShareInfo) {
    val fileName = file.fileName
    val url = file.downloadUrl
    MixDialogBuilder("文件信息").apply {
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    fileName,
                    color = colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                InfoText("大小: ", formatFileSize(file.fileSize))
                if (fileName.parseFileMimeType().toString().startsWith("video/")) {
                    OutlinedButton(
                        {
                            showVideoDialog(url)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("播放视频")
                    }
                }

                if (fileName.parseFileMimeType().toString().startsWith("image/")) {
                    OutlinedButton(
                        {
                            showImageDialog(url)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("查看图片")
                    }
                }
                OutlinedButton(
                    onClick = {
                        val intent =
                            Intent(
                                Intent.ACTION_VIEW,
                                url.toUri()
                            )
                        startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "用其他应用打开")
                }
            }
        }
        setNegativeButton("复制内网地址") {
            url.copyToClipboard()
        }
        setPositiveButton("下载文件") {
            saveFile(url, file.fileName)
            closeDialog()
        }
        show()
    }
}

fun saveFile(url: String, name: String) {
    MixDialogBuilder(
        "下载中",
        autoClose = false
    ).apply {
        setContent {
            val progress = remember {
                ProgressContent()
            }
            AsyncEffect {
                saveFileToStorage(url, name, progress)
                showToast("文件已保存到下载目录")
                closeDialog()
            }
            progress.LoadingContent()
        }
        setNegativeButton("取消") {
            closeDialog()
            showToast("下载已取消")
        }
        show()
    }
}


@Composable
fun FileRow(files: List<MixShareInfo>) {
    ElevatedButton(
        {
            if (files.size == 1) {
                showFileWindow(files.first())
                return@ElevatedButton
            }
            MixDialogBuilder("文件列表").apply {
                setContent {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .heightIn(0.dp, 1000.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        LazyColumn {
                            items(files.size) { index ->
                                val file = files[index]
                                if (index > 0) {
                                    HorizontalDivider()
                                }

                                val color = Color(107, 218, 246, 0)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(color)
                                        .clickable {
                                            showFileWindow(file)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            file.fileName,
                                            color = colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        InfoText("大小: ", formatFileSize(file.fileSize))
                                    }
                                }
                            }
                        }

                    }
                }
                setDefaultNegative("关闭")
                show()
            }
        },
    ) {
        Text("${files.size} 个文件(点击查看)")
    }
}