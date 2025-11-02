package com.donut.mixgram.component.routes.settings.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.GIT_USER_EMAIL
import com.donut.mixgram.util.GIT_USER_NAME
import com.donut.mixgram.util.showToast


fun setGitProfile() {

    var userName by mutableStateOf(GIT_USER_NAME)
    var email by mutableStateOf(GIT_USER_EMAIL)

    MixDialogBuilder("设置Git用户信息").apply {
        setContent {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                    },
                    label = {
                        Text("用户名")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                    },
                    label = {
                        Text("邮箱")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        setPositiveButton("确定") {
            GIT_USER_EMAIL = email.trim()
            GIT_USER_NAME = userName.trim()
            closeDialog()
            showToast("修改成功")
        }
        setDefaultNegative("关闭")
        show()
    }
}
