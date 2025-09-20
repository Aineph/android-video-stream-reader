package com.nicolasfez.sandbox

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.nicolasfez.sandbox.ui.theme.SandboxTheme
import com.nicolasfez.video.VideoStreamReader
import androidx.core.net.toUri
import com.nicolasfez.video.Media3VideoStreamReader
import com.nicolasfez.video.VideoObserver
import java.io.File

class MainActivity : ComponentActivity() {
    @OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoStreamReader: VideoStreamReader = Media3VideoStreamReader(baseContext)

        enableEdgeToEdge()
        setContent {
            SandboxTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PermissionsGuard(
                            permissions = arrayOf(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_VIDEO
                                else android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            ), grantedContent = {
                                val imageBitmap = remember {
                                    mutableStateOf(createBitmap(1, 1).asImageBitmap())
                                }

                                DisposableEffect(Unit) {

                                    val videoObserver = object : VideoObserver {
                                        override fun onFrameReceived(bitmap: Bitmap) {
                                            imageBitmap.value = bitmap.asImageBitmap()
                                        }
                                    }
                                    val videoFile = File(
                                        "${Environment.getExternalStorageDirectory().path}/Movies/video.mp4"
                                    )

                                    if (!videoFile.exists()) {
                                        baseContext.resources.openRawResource(R.raw.video)
                                            .use { input ->
                                                videoFile.outputStream().use { output ->
                                                    input.copyTo(output)
                                                }
                                            }
                                    }

                                    videoStreamReader.addObserver(videoObserver)
                                    videoStreamReader.start(videoFile.toUri())

                                    onDispose {
                                        videoStreamReader.removeObserver(videoObserver)
                                        videoFile.delete()
                                    }

                                }

                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    bitmap = imageBitmap.value,
                                    contentDescription = "Image received as a bitmap object"
                                )
                            }, deniedContent = { requestPermissions ->
                                val context = LocalContext.current

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {

                                    Text(
                                        text = "This demo needs extra permissions to work. Please grant them in the settings.",
                                    )

                                    Spacer(
                                        modifier = Modifier.height(8.dp)
                                    )

                                    Row {
                                        Button(
                                            onClick = {
                                                requestPermissions()
                                            }) {

                                            Text(
                                                text = "Grant permissions"
                                            )

                                        }

                                        Spacer(
                                            modifier = Modifier.height(8.dp)
                                        )

                                    }

                                    Button(
                                        onClick = {
                                            val intent = Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                "package:${context.packageName}".toUri()
                                            )
                                            context.startActivity(intent)
                                        }) {

                                        Text(
                                            text = "Open settings"
                                        )

                                    }

                                }
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SandboxTheme {
        Greeting("Android")
    }
}
