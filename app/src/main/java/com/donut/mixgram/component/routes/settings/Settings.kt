package com.donut.mixgram.component.routes.settings

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
import com.donut.mixgram.component.routes.settings.utils.SettingButton
import com.donut.mixgram.component.routes.settings.utils.setGitProfile
import com.donut.mixgram.component.routes.settings.utils.setUserProfile
import com.donut.mixgram.ui.theme.Theme
import com.donut.mixgram.ui.theme.currentTheme
import com.donut.mixgram.ui.theme.enableAutoDarkMode
import com.donut.mixgram.util.CHAT_USER_NAME
import com.donut.mixgram.util.GIT_USER_NAME
import com.donut.mixgram.util.UNLOCK_PASSWORD
import com.donut.mixgram.util.mixfile.JavaScriptUploader
import com.donut.mixgram.util.mixfile.MIXFILE_UPLOADER
import com.donut.mixgram.util.mixfile.openJavaScriptUploaderWindow
import com.donut.mixgram.util.mixfile.selectUploader
import com.donut.mixgram.util.setUnlockPassword


@OptIn(ExperimentalMaterial3Api::class)
val MixSettings = MixNavPage(
    gap = 10.dp,
    horizontalAlignment = Alignment.CenterHorizontally
) {

    SettingButton(text = "用户信息: ${CHAT_USER_NAME}") {
        setUserProfile()
    }

    SettingButton(text = "Git用户信息: ${GIT_USER_NAME}") {
        setGitProfile()
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
        text = "自动深色模式: ",
        "跟随系统自动切换深色模式",
    ) {
        enableAutoDarkMode = it
    }

    SettingButton(
        text = "APP密码: ",
        description = """
            设置密码后进入需要输入正确的密码解锁
            下次启动APP时会使用此密码加密群组列表
            加密后会删除密码
            忘记密码将会永久丢失群组列表
            建议13位以上,防止暴力破解
            ${if (UNLOCK_PASSWORD.isNotEmpty()) "当前密码: ${UNLOCK_PASSWORD}(已哈希)" else ""}
        """.trimIndent()
    ) {
        setUnlockPassword()
    }

}
