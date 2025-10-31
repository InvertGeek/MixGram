package com.donut.mixgram.component.nav

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.donut.mixfile.ui.routes.settings.MixSettings
import com.donut.mixgram.component.routes.group_list.GroupList

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavContent(innerPaddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)
            .fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val controller = getNavController()

        NavHost(
            navController = controller,
            startDestination = GroupList.name
        ) {
            GroupList(this)

            MixSettings(this)

        }
    }
}