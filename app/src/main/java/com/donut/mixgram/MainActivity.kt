package com.donut.mixgram

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.donut.mixgram.component.MainContent
import com.donut.mixgram.util.objects.MixActivity

class MainActivity : MixActivity("main") {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }

}

