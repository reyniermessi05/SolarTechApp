package com.example.solarapp.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfViewerScreen(fileName: String, initialPage: Int = 1) {
    val context = LocalContext.current
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }

    // Use a mutex because PdfRenderer isn't thread safe and we can only open one page at a time
    val renderMutex = remember { Mutex() }
    val listState = rememberLazyListState()

    DisposableEffect(fileName) {
        try {
            val file = File(context.cacheDir, fileName)
            if (!file.exists()) {
                context.assets.open(fileName).use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            if (fd != null) {
                fileDescriptor = fd
                val renderer = PdfRenderer(fd)
                pdfRenderer = renderer
                pageCount = renderer.pageCount
            } else {
                error = "Could not open file descriptor"
            }
        } catch (e: Exception) {
            error = "Could not load PDF: ${e.message}"
            e.printStackTrace()
        }

        onDispose {
            try {
                pdfRenderer?.close()
            } catch (e: Exception) { e.printStackTrace() }
            try {
                fileDescriptor?.close()
            } catch (e: Exception) { e.printStackTrace() }
            pdfRenderer = null
            fileDescriptor = null
            pageCount = 0
        }
    }

    LaunchedEffect(pageCount, initialPage) {
        if (pageCount > 0) {
            try {
                // Strict bounds checking before calculating target index
                val safeInitialPage = if (initialPage - 1 < 0 || initialPage - 1 >= pageCount) 1 else initialPage
                val targetIndex = safeInitialPage - 1

                // Extra safety validation
                if (targetIndex in 0 until pageCount) {
                    listState.scrollToItem(targetIndex)
                } else {
                    android.util.Log.e("PdfViewerScreen", "Invalid page target: $targetIndex")
                }
            } catch (e: Exception) {
                android.util.Log.e("PdfViewerScreen", "Failed to scroll to page: ${e.message}")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (error != null) {
            Text(text = error!!)
        } else if (pdfRenderer != null && pageCount > 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                items(pageCount) { index ->
                    PdfPage(
                        renderer = pdfRenderer,
                        pageIndex = index,
                        renderMutex = renderMutex
                    )
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PdfPage(
    renderer: PdfRenderer?,
    pageIndex: Int,
    renderMutex: Mutex
) {
    if (renderer == null) return
    val density = LocalDensity.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex, renderer) {
        withContext(Dispatchers.IO) {
            renderMutex.withLock {
                try {
                    if (pageIndex !in 0 until renderer.pageCount) return@withLock
                    val page = renderer.openPage(pageIndex)

                    // Render at high resolution
                    val width = (page.width * density.density * 2).toInt()
                    val height = (page.height * density.density * 2).toInt()

                    val renderedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    renderedBitmap.eraseColor(android.graphics.Color.WHITE)

                    page.render(renderedBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap = renderedBitmap
                    page.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    if (bitmap != null) {
        ZoomableBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(bitmap!!.width.toFloat() / bitmap!!.height.toFloat())
        ) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "PDF Page ${pageIndex + 1}",
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // Placeholder while loading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        scale = (scale * zoom).coerceIn(1f, 5f)
                        if (scale > 1f) {
                            val newOffset = offset + pan
                            offset = newOffset
                            // Consume the event so the LazyColumn doesn't intercept it when zoomed in
                            event.changes.forEach { it.consume() }
                        } else {
                            offset = Offset.Zero
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
    ) {
        content()
    }
}
