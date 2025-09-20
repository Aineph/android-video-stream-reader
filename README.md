# Android Video Stream Reader

Trying to read external video streams can be somehow difficult on Android.
You need to have a clear understanding of how the Media API works and it's not really well
documented for now.

This project has been created to show you how to simply process a video
stream using different approaches.

# Installation

Clone the repository and open the project using **Android Studio**.

Once opened, go to *File* > *Sync Project with Gradle Files*.
Wait a couple of minutes and you will be able to build and run the app.

Alternatively, you can use the command lines:

```bash
./gradlew
./gradlew assembleDebug
./gradlew installDebug
```

# Architecture

This demonstration project encapsulates the processing of video streams with both JavaCV and the
standard Android Media Api (ExoPlayer).
It's composed of two distinct modules:

- **app** which is the application installed on your phone / tablet.
- **video** containing our JavaCV / Media API encapsulation.

## App Module

On the **app** module, we declare a single activity containing an ImageView. This view is rendering an
ImageBitmap object which is constantly updated when new frames are decoded and received.

To run the demo, the app requires the READ_MEDIA_VIDEO permission for devices running Android 12
Tiramisu and later. Therefore, a PermissionsGuard component handles the permissions request logic.

## Video Module

The **video** module defines the encapsulation we use for receiving frames from either JavaCV or
ExoPlayer. It defines an abstract class named **VideoStreamReader** containing two pure methods, start
and stop for starting and stopping a stream processing. It also handles a logic for managing
observers that will retrieve the frames as bitmaps.

A VideoObserver interface is defined with a callback function that will be called when a new frame
is available.

# Compatibility

The project is currently compatible with any Android device since **API level 21** (Android 5.0
Lollipop).
