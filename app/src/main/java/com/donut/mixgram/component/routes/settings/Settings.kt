package com.donut.mixfile.ui.routes.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInVertically
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.donut.mixgram.component.common.CommonSwitch
import com.donut.mixgram.component.common.MixDialogBuilder
import com.donut.mixgram.component.common.SingleSelectItemList
import com.donut.mixgram.component.nav.MixNavPage
import com.donut.mixgram.component.nav.NavTitle
import com.donut.mixgram.component.routes.settings.utils.SettingButton
import com.donut.mixgram.component.routes.settings.utils.setUserProfile
import com.donut.mixgram.ui.theme.Theme
import com.donut.mixgram.ui.theme.currentTheme
import com.donut.mixgram.ui.theme.enableAutoDarkMode
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.mixfile.JavaScriptUploader
import com.donut.mixgram.util.mixfile.MIXFILE_UPLOADER
import com.donut.mixgram.util.mixfile.openJavaScriptUploaderWindow
import com.donut.mixgram.util.mixfile.selectUploader


@OptIn(ExperimentalMaterial3Api::class)
val MixSettings = MixNavPage(
    gap = 10.dp,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    SettingButton(text = "用户信息: ${CHAT_USER_NAME}") {
        setUserProfile()
    }
    SettingButton(text = "文件上传线路: $MIXFILE_UPLOADER") {
        selectUploader()
    }
    AnimatedVisibility(
        MIXFILE_UPLOADER.contentEquals(JavaScriptUploader.name),
        enter = slideInVertically(),
        exit = shrinkOut()
    ) {
        SettingButton(text = "JS自定义线路设置: ") {
            openJavaScriptUploaderWindow()
        }
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
