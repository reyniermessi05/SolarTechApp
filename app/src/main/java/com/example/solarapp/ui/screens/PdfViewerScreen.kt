package com.example.solarapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfViewerScreen(fileName: String, initialPage: Int = 1) {
    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(fileName) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, fileName)
                if (!file.exists()) {
                    context.assets.open(fileName).use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    pdfFile = file
                }
            } catch (e: java.io.FileNotFoundException) {
                withContext(Dispatchers.Main) {
                    val errorMsg = "Archivo no encontrado: $fileName"
                    error = errorMsg
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error = "Could not load PDF: ${e.message}"
                    e.printStackTrace()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (error != null) {
            Text(text = error!!)
        } else if (pdfFile != null) {
            AndroidView(
                factory = { ctx ->
                    PDFView(ctx, null).apply {
                        fromFile(pdfFile)
                            .defaultPage(if (initialPage > 0) initialPage - 1 else 0)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .enableAnnotationRendering(false)
                            .scrollHandle(DefaultScrollHandle(ctx))
                            .spacing(10) // in dp
                            .load()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator()
        }
    }
}
