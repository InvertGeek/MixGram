package com.donut.mixgram.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixgram.component.nav.NavComponent
import com.donut.mixgram.component.routes.settings.utils.setStringValue
import com.donut.mixgram.ui.theme.MainTheme
import com.donut.mixgram.util.ENCRYPTED_DATA
import com.donut.mixgram.util.decryptGroups
import com.donut.mixgram.util.showToast


fun clearData() {
    setStringValue(
        "清除数据",
        "", "请输入确认",
        {
            if (!it.trim().contentEquals("确认")) {
                showToast("请输入确认!")
                return@setStringValue
            }
            ENCRYPTED_DATA = ""
            showToast("清除成功")
        },
        1,
        "忘记密码后没有任何方法可以恢复群组,只能清空群组列表"
    )
}

@Composable
fun UnLockScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var password by remember { mutableStateOf("") }
            Text("群组列表已加密,请输入密码解锁", color = colorScheme.primary)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it.trim()
                },
                label = {
                    Text("请输入密码")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                {
                    val result = decryptGroups(password)
                    if (!result) {
                        showToast("密码错误")
                        return@Button
                    }
                    showToast("解锁成功")
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("确认解锁")
            }
            OutlinedButton(
                {
                    clearData()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("忘记密码")
            }
        }
    }
}

@Composable
fun MainContent() {

    MainTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                ENCRYPTED_DATA.isNotEmpty(),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }) {
                UnLockScreen()
                return@AnimatedVisibility
            }

            AnimatedVisibility(
                visible = ENCRYPTED_DATA.isEmpty(),
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it }
            ) {
                NavComponent()
                return@AnimatedVisibility
            }
        }
    }

}


