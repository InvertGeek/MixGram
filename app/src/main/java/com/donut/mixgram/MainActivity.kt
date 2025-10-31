package com.donut.mixgram

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.donut.mixgram.component.MainContent
import com.donut.mixgram.util.file.MixFileSelector
import com.donut.mixgram.util.objects.MixActivity

class MainActivity : MixActivity("main") {
    companion object {
        lateinit var mixFileSelector: MixFileSelector
    }

    override fun onDestroy() {
        super.onDestroy()
        mixFileSelector.unregister()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mixFileSelector = MixFileSelector(this)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }

}

