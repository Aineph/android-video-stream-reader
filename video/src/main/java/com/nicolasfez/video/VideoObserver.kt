package com.nicolasfez.video

import android.graphics.Bitmap

interface VideoObserver {

    fun onFrameReceived(bitmap: Bitmap)

}
