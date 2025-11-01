package com.donut.mixgram.util.file

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.donut.mixgram.util.objects.MixActivity
import kotlinx.coroutines.suspendCancellableCoroutine

class MixFileSelector(activity: MixActivity) {
    private var fileSelector: ActivityResultLauncher<Array<String>>
    private var callback: (uri: List<Uri>) -> Unit = { }

    init {
        fileSelector =
            activity.registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
                callback(it)
            }
    }

    fun unregister() {
        fileSelector.unregister()
    }

    suspend fun openSelect() = suspendCancellableCoroutine { cont ->
        this.callback = {
            cont.resumeWith(Result.success(it))
        }
        fileSelector.launch(arrayOf("*/*"))
    }


    fun openSelect(
        array: Array<String> = arrayOf("*/*"),
        callback: (uri: List<Uri>) -> Unit,
    ) {
        this.callback = callback
        fileSelector.launch(array)
    }
}