package com.donut.mixgram.component.routes

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.donut.mixfile.server.core.utils.ignoreError
import com.donut.mixgram.app
import com.donut.mixgram.component.common.CommonSwitch
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.component.nav.MixNavPage
import com.donut.mixgram.updateChecker
import com.donut.mixgram.util.AsyncEffect
import com.donut.mixgram.util.cachedMutableOf
import com.donut.mixgram.util.getAppVersionName
import com.donut.mixgram.util.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

var autoCheckUpdate by cachedMutableOf(true, "auto_check_update")


@OptIn(ExperimentalLayoutApi::class)
val About = MixNavPage(
    gap = 10.dp,
    horizontalAlignment = Alignment.CenterHorizontally
) {

    OutlinedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = "当前版本: ${getAppVersionName(LocalContext.current)}",
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                color = colorScheme.primary,
                text = "项目地址: https://github.com/InvertGeek/MixGram",
                modifier = Modifier.clickable {
                    openGithubLink()
                }
            )

            OutlinedButton(onClick = {
                showUpdateDialog()
            }) {
                Text(text = "检查更新")
            }
            CommonSwitch(
                checked = autoCheckUpdate,
                text = "启动时自动检查更新:"
            ) {
                autoCheckUpdate = it
            }

        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            color = Color.Gray,
            text = """
        MixGram可将任何Git仓库作为聊天服务器
        您发送的所有信息和文件都会使用AES-GCM算法加密,
        创建群组时会动态随机生成秘钥进行加密
        只有知道此秘钥的用户才能解密仓库中的commit信息
        只要不泄漏群组分享码,聊天信息和文件内容是无法被任何人得知的
        256位密钥可确保即使是在未来使用量子计算机也无法破解
    """.trimIndent()
        )
    }
}

fun openGithubLink() {
    val intent =
        Intent(
            Intent.ACTION_VIEW,
            "https://github.com/InvertGeek/MixGram".toUri()
        )
    startActivity(intent)
}

suspend fun checkForUpdates(latest: String? = null, showUpdatedDialog: Boolean = false) {
    val latestVersion =
        latest ?: withContext(Dispatchers.IO) { ignoreError { updateChecker.latestVersion } }
    if (latestVersion == null) {
        return
    }
    if (latestVersion.contentEquals(getAppVersionName(app))) {
        if (showUpdatedDialog) {
            MixDialogBuilder("已是最新版本").apply {
                setPositiveButton("确定") { closeDialog() }
                show()
            }
        }
        return
    }
    MixDialogBuilder("有新版本: ${latestVersion}").apply {
        setPositiveButton("下载") { openGithubLink() }
        show()
    }
}

fun showUpdateDialog() {
    MixDialogBuilder("检查更新中").apply {
        setContent {
            var latestVersion: String? by remember { mutableStateOf(null) }

            AsyncEffect {
                ignoreError {
                    latestVersion = updateChecker.latestVersion
                }
                withContext(Dispatchers.Main) {
                    closeDialog()
                    checkForUpdates(latestVersion, showUpdatedDialog = true)
                }
            }
            if (latestVersion == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }

                return@setContent
            }
        }
        setDefaultNegative()
        show()
    }
}