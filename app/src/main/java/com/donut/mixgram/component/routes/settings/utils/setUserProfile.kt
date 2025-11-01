package com.donut.mixgram.component.routes.settings.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import com.donut.mixgram.activity.chat_page.components.message.Avatar
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.CHAT_USER_AVATAR
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.mixfile.selectFilesUpload
import kotlinx.coroutines.launch

fun setUserProfile() {
    MixDialogBuilder("设置用户信息").apply {
        setContent {
            val scope = rememberCoroutineScope()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("头像: ")
                    Avatar(CHAT_USER_NAME, shareInfo = resolveMixShareInfo(CHAT_USER_AVATAR)) {}
                    OutlinedButton({
                        scope.launch {
                            val result = selectFilesUpload(true, sizeLimit = 1024 * 1024)
                            if (result.isNotEmpty()) {
                                CHAT_USER_AVATAR = result.first().toString()
                            }
                        }
                    }) {
                        Text("选择文件")
                    }
                }
                SettingButton(text = "用户名: $CHAT_USER_NAME") {
                    setChatUserName()
                }
            }
        }
        setDefaultNegative("关闭")
        show()
    }

}