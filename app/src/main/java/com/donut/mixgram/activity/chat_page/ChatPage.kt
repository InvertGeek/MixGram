package com.donut.mixgram.activity.chat_page

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixgram.activity.chat_page.components.ChatScreen
import com.donut.mixgram.activity.chat_page.components.SendMessage
import com.donut.mixgram.ui.theme.MainTheme
import com.donut.mixgram.util.CHAT_GROUPS
import com.donut.mixgram.util.objects.MixActivity

class ChatPage : MixActivity("chat_page") {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupName = intent.getStringExtra("group")
        val group = CHAT_GROUPS.find { it.name.contentEquals(groupName) }
        setContent {
            MainTheme {
                val focusManager = LocalFocusManager.current
                Scaffold(
                    bottomBar = {
                        if (group == null) {
                            return@Scaffold
                        }
                        SendMessage(group)
                    },
                    modifier = Modifier
                        .imePadding()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                focusManager.clearFocus()
                            }
                        }
                ) { padValue ->
                    Column(
                        modifier = Modifier
                            .padding(padValue)
                    ) {
                        if (group == null) {
                            Text("未找到群组")
                            return@Scaffold
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            IconButton({
                                finish()
                            }) {
                                Icon(
                                    Icons.Filled.ArrowBackIosNew,
                                    "Back",
//                                    tint = colorScheme.onPrimary,
                                )
                            }
                            Text(
                                modifier = Modifier.padding(10.dp),
//                                color = colorScheme.onPrimary,
                                text = group.name,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        ChatScreen(group)
                    }
                }
            }
        }
    }
}