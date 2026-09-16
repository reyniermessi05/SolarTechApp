package com.example.solarapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfViewerScreen(fileName: String) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val density = LocalDensity.current

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    DisposableEffect(fileName) {
        var fileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null

        try {
            val file = File(context.cacheDir, fileName)
            if (!file.exists()) {
                context.assets.open(fileName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            if (fileDescriptor != null) {
                pdfRenderer = PdfRenderer(fileDescriptor)
                if (pdfRenderer.pageCount > 0) {
                    currentPage = pdfRenderer.openPage(0)

                    // Higher resolution for better zoom quality
                    val width = (currentPage.width * density.density * 2).toInt()
                    val height = (currentPage.height * density.density * 2).toInt()

                    val renderedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    // Fill background with white
                    renderedBitmap.eraseColor(android.graphics.Color.WHITE)

                    currentPage.render(renderedBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap = renderedBitmap
                } else {
                    error = "PDF is empty"
                }
            }
        } catch (e: Exception) {
            error = "Could not load PDF: ${e.message}"
            e.printStackTrace()
        }

        onDispose {
            currentPage?.close()
            pdfRenderer?.close()
            fileDescriptor?.close()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)

                    // Simple panning constraint (could be improved to constrain fully within bounds)
                    val newOffset = offset + pan
                    offset = if (scale > 1f) newOffset else Offset.Zero
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (error != null) {
            Text(text = error!!)
        } else if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "PDF Page",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        } else {
            Text(text = "Loading PDF...")
        }
    }
}
