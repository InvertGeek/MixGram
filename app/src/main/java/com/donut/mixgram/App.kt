package com.donut.mixgram


import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Looper
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import com.donut.mixgram.util.encryptGroups
import com.donut.mixgram.util.getAppVersionName
import com.donut.mixgram.util.loopTask
import com.donut.mixgram.util.mixfile.server
import com.donut.mixgram.util.objects.MixActivity
import com.donut.mixgram.util.objects.UpdateChecker
import com.donut.mixgram.util.showError
import com.donut.mixgram.util.showErrorDialog
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient

val appScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

lateinit var kv: MMKV

private lateinit var innerApp: Application


val currentActivity: MixActivity?
    get() {
        return MixActivity.firstActiveActivity()
    }

val app: Application
    get() = innerApp

lateinit var updateChecker: UpdateChecker

class App : Application(), ImageLoaderFactory {


    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        kv = MMKV.defaultMMKV()
        kv.enableCompareBeforeSet()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            showError(e)
            if (Looper.myLooper() == null) {
                return@setDefaultUncaughtExceptionHandler
            }
            showErrorDialog(e)
        }
        innerApp = this
        server.start(false)
        updateChecker = UpdateChecker("InvertGeek", "MixGram", getAppVersionName(this))
        appScope.loopTask(1000 * 60 * 10) {
            kv.clearMemoryCache()
            kv.trim()
        }
        encryptGroups()

    }


    override fun newImageLoader(): ImageLoader {
        return genImageLoader(this)
    }


}

fun genImageLoader(
    context: Context,
    initializer: () -> OkHttpClient = { OkHttpClient() },
): ImageLoader {
    return ImageLoader.Builder(context).components {
        if (Build.VERSION.SDK_INT >= 28) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
        add(SvgDecoder.Factory())
        add(VideoFrameDecoder.Factory())
    }.okHttpClient(initializer)
        .crossfade(true).build()
}