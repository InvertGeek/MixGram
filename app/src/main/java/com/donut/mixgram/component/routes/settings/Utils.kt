package com.donut.mixgram.component.routes.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.showToast

fun setChatUserName() {
    MixDialogBuilder("设置聊天用户名").apply {
        var userName by mutableStateOf(CHAT_USER_NAME)
        setContent {
            OutlinedTextField(
                value = userName,
                onValueChange = {
                    userName = it
                },
                maxLines = 1,
                label = { Text(text = "聊天用户名") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        setDefaultNegative()
        setPositiveButton("确定") {
            CHAT_USER_NAME = userName.trim().take(20)
            showToast("设置成功")
            closeDialog()
        }
        show()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingButton(
    text: String,
    buttonText: String = "设置",
    description: String = "",
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            OutlinedButton(onClick = onClick) {
                Text(text = buttonText)
            }
        }
        if (description.isNotEmpty()) {
            Text(
                text = description,
                modifier = Modifier
                    .fillMaxWidth(),
//                    .padding(10.dp, 0.dp),
                color = Color(0xFF9E9E9E),
                fontSize = 14.sp
            )
        }
    }
}