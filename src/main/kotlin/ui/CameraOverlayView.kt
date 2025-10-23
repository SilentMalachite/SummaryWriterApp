package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image as SkiaImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import models.TextMessage
import services.CameraService
import services.TextService
import java.awt.image.BufferedImage

@Composable
fun CameraOverlayView(
    cameraService: CameraService,
    textService: TextService,
    modifier: Modifier = Modifier,
    fontSize: Float = 24f
) {
    var cameraFrame by remember { mutableStateOf<ImageBitmap?>(null) }
    val latestText by textService.latestText.collectAsState()
    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            isInitialized = cameraService.initialize()
            while (isInitialized) {
                val frame = cameraService.captureFrame()
                frame?.let {
                    cameraFrame = it.toImageBitmap()
                }
                delay(33)
            }
        } catch (e: Exception) {
            println("カメラフィードエラー: ${e.message}")
            isInitialized = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (cameraFrame != null) {
            Image(
                bitmap = cameraFrame!!,
                contentDescription = "Camera feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isInitialized) "カメラ読み込み中..." else "カメラが利用できません",
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }

        if (latestText.text.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            ) {
                Text(
                    text = latestText.text,
                    color = Color.White,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraService.close()
        }
    }
}

private fun BufferedImage.toImageBitmap(): ImageBitmap {
    val bytes = java.io.ByteArrayOutputStream().use { baos ->
        javax.imageio.ImageIO.write(this, "png", baos)
        baos.toByteArray()
    }
    return SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
}
