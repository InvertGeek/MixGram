package com.donut.mixgram.util.mixfile

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.utils.StreamContent
import com.donut.mixfile.server.core.utils.encodeURL
import com.donut.mixfile.server.core.utils.extensions.kb
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import com.donut.mixfile.server.core.utils.sanitizeFileName
import com.donut.mixgram.app
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.currentActivity
import com.donut.mixgram.util.AsyncEffect
import com.donut.mixgram.util.errorDialog
import com.donut.mixgram.util.formatFileSize
import com.donut.mixgram.util.getFileName
import com.donut.mixgram.util.getFileSize
import com.donut.mixgram.util.objects.ProgressContent
import com.donut.mixgram.util.showToast
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.streams.asByteWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

suspend fun selectFilesUpload(
    single: Boolean = false,
    sizeLimit: Long? = null
): List<MixShareInfo> {
    val files = currentActivity?.fileSelector?.openSelect() ?: listOf()
    if (files.isEmpty()) {
        return listOf()
    }

    return suspendCancellableCoroutine { cont ->
        MixDialogBuilder(
            "上传中",
            autoClose = false
        ).apply {
            setContent {
                var progressContent by remember {
                    mutableStateOf(ProgressContent("上传中"))
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    progressContent.LoadingContent()
                }

                var uploaded by remember { mutableStateOf(listOf<MixShareInfo>()) }

                if (!single && files.size > 1) {
                    Text("已上传: ${uploaded.size}/${files.size}")
                }

                AsyncEffect {
                    files.forEach { uri ->
                        val fileSize = errorDialog("读取文件失败") {
                            uri.getFileSize()
                        } ?: return@AsyncEffect
                        if (sizeLimit != null && fileSize > sizeLimit) {
                            showToast("超过大小限制: ${formatFileSize(sizeLimit)}")
                            closeDialog()
                            return@AsyncEffect
                        }
                        withContext(Dispatchers.Main) {
                            progressContent = ProgressContent("上传中", showLoading = false)
                        }
                        val resolver = app.contentResolver
                        val fileStream = resolver.openInputStream(uri)
                        if (fileStream == null) {
                            showToast("打开文件失败")
                            closeDialog()
                            return@AsyncEffect
                        }
                        val stream = StreamContent(fileStream, fileSize)
                        val fileCode =
                            putUploadFile(stream, uri.getFileName(), false, progressContent)
                        if (fileCode.isEmpty()) {
                            showToast("上传失败")
                            closeDialog()
                            return@AsyncEffect
                        }
                        val shareInfo = resolveMixShareInfo(fileCode)
                        if (shareInfo == null) {
                            showToast("上传失败")
                            closeDialog()
                            return@AsyncEffect
                        }
                        withContext(Dispatchers.Main) {
                            uploaded += shareInfo
                        }
                        if (single && uploaded.isNotEmpty()) {
                            return@forEach
                        }
                    }
                    cont.resumeWith(Result.success(uploaded))
                    closeDialog()
                    showToast("上传成功")
                }
            }
            setDefaultNegative("取消")
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

val MixShareInfo.downloadUrl: String
    get() {
        return URLBuilder("http://127.0.0.1:${server.serverPort}/api/download/${this.fileName.encodeURL()}").apply {
            parameters.apply {
                append("s", this@downloadUrl.toString())
            }
        }.toString()
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

suspend fun saveFileToStorage(
    url: String,
    displayName: String,
    progress: ProgressContent,
    directory: String = Environment.DIRECTORY_DOWNLOADS,
    storeUri: Uri = MediaStore.Files.getContentUri("external"),
): Uri? {
    val resolver = app.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName.sanitizeFileName())
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
        put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
    }


    val fileUri = resolver.insert(storeUri, contentValues)
    coroutineContext.job.invokeOnCompletion { throwable ->
        if (throwable !is CancellationException) {
            return@invokeOnCompletion
        }
        if (fileUri == null) {
            return@invokeOnCompletion
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            resolver.delete(fileUri, null)
        }
    }
    if (fileUri == null) {
        return null
    }
    localClient.prepareGet {
        url(url)
        onDownload(progress.ktorListener)
    }.execute {
        if (!it.status.isSuccess()) {
            val text =
                if ((it.contentLength() ?: 1024L.kb) < 500L.kb) it.bodyAsText() else "未知错误"
            throw Exception("下载失败: ${text}")
        }
        resolver.openOutputStream(fileUri)?.use { output ->
            it.bodyAsChannel().copyAndClose(output.asByteWriteChannel())
        }
    }
    return fileUri
}

