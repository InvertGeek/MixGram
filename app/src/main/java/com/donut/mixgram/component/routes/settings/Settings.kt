package com.donut.mixfile.ui.routes.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.donut.mixgram.component.common.CommonSwitch
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.component.common.SingleSelectItemList
import com.donut.mixgram.component.nav.MixNavPage
import com.donut.mixgram.component.routes.settings.SettingButton
import com.donut.mixgram.component.routes.settings.setChatUserName
import com.donut.mixgram.ui.theme.Theme
import com.donut.mixgram.ui.theme.currentTheme
import com.donut.mixgram.ui.theme.enableAutoDarkMode
import com.donut.mixgram.util.CHAT_USER_NAME


@OptIn(ExperimentalMaterial3Api::class)
val MixSettings = MixNavPage(
    gap = 10.dp,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    SettingButton(text = "聊天用户名: ${CHAT_USER_NAME}") {
        setChatUserName()
    }

    SettingButton(text = "颜色主题: ") {
        MixDialogBuilder("颜色主题").apply {
            setContent {
                SingleSelectItemList(
                    items = Theme.entries,
                    getLabel = { it.label },
                    currentOption = Theme.entries.firstOrNull {
                        it.name == currentTheme
                    } ?: Theme.DEFAULT
                ) { option ->
                    currentTheme = option.name
                    closeDialog()
                }
            }
            show()
        }
    }
    CommonSwitch(
        checked = enableAutoDarkMode,
        text = "自动深色模式:",
        "跟随系统自动切换深色模式",
    ) {
        enableAutoDarkMode = it
    }

}
