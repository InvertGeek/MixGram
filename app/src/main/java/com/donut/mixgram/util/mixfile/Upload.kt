package com.donut.mixgram.util.mixfile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.donut.mixfile.server.core.utils.StreamContent
import com.donut.mixgram.MainActivity
import com.donut.mixgram.app
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.AsyncEffect
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.getFileName
import com.donut.mixgram.util.getFileSize
import com.donut.mixgram.util.objects.ProgressContent
import com.donut.mixgram.util.showToast
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

fun selectFile(types: Array<String> = arrayOf("*/*")) {
    MainActivity.mixFileSelector.openSelect(types) { uri ->
        MixDialogBuilder(
            "上传中",
            autoClose = false
        ).apply {
            setContent {
                val progressContent = remember {
                    ProgressContent("上传中")
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    progressContent.LoadingContent()
                }
                AsyncEffect {
                    val resolver = app.contentResolver
                    val fileSize = errorDialog("读取文件失败") {
                        uri.getFileSize()
                    } ?: return@AsyncEffect
                    val fileStream = resolver.openInputStream(uri)
                    if (fileStream == null) {
                        showToast("打开文件失败")
                        return@AsyncEffect
                    }
                    val stream = StreamContent(fileStream, fileSize)
                    val fileCode = putUploadFile(stream, uri.getFileName(), false, progressContent)
                    if (fileCode.isEmpty()) {
                        showToast("上传失败")
                        return@AsyncEffect
                    }

                    showToast("上传成功!")
                    closeDialog()
                }
            }
            setDefaultNegative()
            show()
        }
    }
}

val localClient = HttpClient(OkHttp).config {
    install(HttpTimeout) {
        requestTimeoutMillis = 1000 * 60 * 60 * 24 * 30L
        socketTimeoutMillis = 1000 * 60 * 60
        connectTimeoutMillis = 1000 * 60 * 60
    }
}

suspend fun putUploadFile(
    data: Any?,
    name: String,
    add: Boolean = true,
    progressContent: ProgressContent = ProgressContent(),
): String {
    return errorDialog<String>("上传失败") {
        val response = localClient.put {
            url("http://127.0.0.1:${server.serverPort}/api/upload")
            onUpload(progressContent.ktorListener)
            parameter("name", name)
            parameter("add", add)
            setBody(data)
        }
        val message = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw Exception("上传失败: $message")
        }
        return message
    } ?: ""
}

