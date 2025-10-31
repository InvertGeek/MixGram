package com.donut.mixgram.component

import androidx.compose.runtime.Composable
import com.donut.mixgram.component.nav.NavComponent
import com.donut.mixgram.ui.theme.MainTheme


@Composable
fun MainContent() {
    MainTheme {
        NavComponent()
    }
}


