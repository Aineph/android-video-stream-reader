package com.nicolasfez.video;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class FFmpegVideoStreamReader extends VideoStreamReader {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Override
    public void start(@NotNull Uri uri) {
        stop();
        isRunning.set(true);
        new Thread(() -> {
            try (
                    FFmpegFrameGrabber ffmpegFrameGrabber = new FFmpegFrameGrabber(uri.getPath());
                    AndroidFrameConverter androidFrameConverter = new AndroidFrameConverter()
            ) {
                ffmpegFrameGrabber.start();
                while (isRunning.get()) {
                    try {
                        Frame frame = ffmpegFrameGrabber.grabImage();

                        if (frame == null) {
                            ffmpegFrameGrabber.setFrameNumber(0);
                        } else {
                            Bitmap bitmap = androidFrameConverter.convert(frame);

                            notifyObservers(bitmap);
                            frame.close();
                        }
                    } catch (FrameGrabber.Exception e) {
                        Log.e("FFmpegVideoStreamReader", e.toString());
                    }
                }
            } catch (FrameGrabber.Exception e) {
                Log.e("FFmpegVideoStreamReader", e.toString());
            }
        }).start();
    }

    @Override
    public void stop() {
        isRunning.set(false);
    }

}
