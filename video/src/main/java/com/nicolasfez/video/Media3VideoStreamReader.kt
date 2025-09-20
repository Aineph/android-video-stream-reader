package com.nicolasfez.video

import android.content.Context
import android.graphics.PixelFormat
import android.media.ImageReader
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.media3.common.C
import androidx.media3.common.ColorInfo
import androidx.media3.common.DebugViewProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.SurfaceInfo
import androidx.media3.common.Tracks
import androidx.media3.common.VideoFrameProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.DefaultGlObjectsProvider
import androidx.media3.effect.DefaultVideoFrameProcessor
import androidx.media3.exoplayer.ExoPlayer
import java.util.concurrent.Executors

const val OUTPUT_WIDTH = 1920
const val OUTPUT_HEIGHT = 1080

@UnstableApi
class Media3VideoStreamReader(context: Context) : VideoStreamReader() {

    private val player = ExoPlayer.Builder(context).build()
    private val imageReader =
        ImageReader.newInstance(OUTPUT_WIDTH, OUTPUT_HEIGHT, PixelFormat.RGBA_8888, 2)
    private val executorService = Executors.newSingleThreadExecutor()
    private val videoFrameProcessorFactory =
        DefaultVideoFrameProcessor.Factory.Builder().setExecutorService(
            executorService
        ).setGlObjectsProvider(DefaultGlObjectsProvider()).build()
    private var videoFrameProcessor: DefaultVideoFrameProcessor? = null

    init {
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.addListener(
            object : Player.Listener {
                override fun onTracksChanged(tracks: Tracks) {
                    tracks.groups.forEach { group ->
                        if (group.type == C.TRACK_TYPE_VIDEO) {
                            for (i in 0 until group.length) {
                                val format = group.getTrackFormat(i)

                                try {
                                    player.clearVideoSurface()
                                    videoFrameProcessor?.release()
                                    videoFrameProcessor = videoFrameProcessorFactory.create(
                                        context,
                                        DebugViewProvider.NONE,
                                        ColorInfo.SDR_BT709_LIMITED,
                                        true,
                                        executorService,
                                        object : VideoFrameProcessor.Listener {}
                                    ).also { videoFrameProcessor ->
                                        videoFrameProcessor.setOnInputSurfaceReadyListener {
                                            videoFrameProcessor.setOutputSurfaceInfo(
                                                SurfaceInfo(
                                                    imageReader.surface,
                                                    OUTPUT_WIDTH,
                                                    OUTPUT_HEIGHT
                                                )
                                            )
                                            videoFrameProcessor.registerInputStream(
                                                VideoFrameProcessor.INPUT_TYPE_SURFACE_AUTOMATIC_FRAME_REGISTRATION,
                                                format.buildUpon()
                                                    .setColorInfo(ColorInfo.SDR_BT709_LIMITED)
                                                    .build(),
                                                mutableListOf(),
                                                0
                                            )
                                            player.setVideoSurface(videoFrameProcessor.inputSurface)
                                            player.play()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("Media3VideoStreamReader", e.toString())
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    private fun acquireLatestImage(reader: ImageReader) =
        reader.acquireLatestImage().let { image ->
            if (image == null) return@let createBitmap(1, 1)
            val planes = image.planes
            val buffer = planes.first().buffer
            val width = image.width
            val height = image.height
            val imageBuffer = buffer.rewind()
            val pixelStride = planes.first().pixelStride
            val rowStride = planes.first().rowStride
            val rowPadding = rowStride - (pixelStride * width)

            createBitmap((rowPadding / pixelStride) + width, height).let { bitmap ->
                bitmap.copyPixelsFromBuffer(imageBuffer)
                image.close()
                bitmap
            }
        }

    override fun start(uri: Uri) {
        stop()
        imageReader.setOnImageAvailableListener({ reader ->
            notifyObservers(acquireLatestImage(reader))
        }, Handler(Looper.getMainLooper()))
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
    }

    override fun stop() {
        player.stop()
    }

}