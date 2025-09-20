package com.nicolasfez.video

import android.graphics.Bitmap
import android.net.Uri

abstract class VideoStreamReader {

    val observerList = mutableListOf<VideoObserver>()

    abstract fun start(uri: Uri)

    abstract fun stop()

    fun notifyObservers(bitmap: Bitmap) {
        observerList.forEach { it.onFrameReceived(bitmap) }
    }

    fun addObserver(observer: VideoObserver) {
        observerList.add(observer)
    }

    fun removeObserver(observer: VideoObserver) {
        observerList.remove(observer)
    }

}
