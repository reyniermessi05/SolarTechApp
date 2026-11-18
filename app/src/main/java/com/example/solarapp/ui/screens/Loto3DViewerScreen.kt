package com.example.solarapp.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Loto3DViewerScreen(
    equipmentName: String,
    modelFileName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assetLoader = remember(context) {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }
    val viewerUrl = remember(modelFileName) {
        "https://appassets.androidplatform.net/assets/loto_viewer.html?model=${Uri.encode(modelFileName)}"
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                setBackgroundColor(Color.parseColor("#121212"))

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.builtInZoomControls = false
                settings.displayZoomControls = false

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView,
                        request: WebResourceRequest
                    ): WebResourceResponse? {
                        return assetLoader.shouldInterceptRequest(request.url)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val escapedFileName = modelFileName
                            .replace("\\", "\\\\")
                            .replace("'", "\\'")
                        view?.evaluateJavascript(
                            "if (typeof loadEquipmentModel === 'function') { loadEquipmentModel('$escapedFileName'); }",
                            null
                        )
                    }
                }

                loadUrl(viewerUrl)
            }
        },
        update = { webView ->
            if (webView.url != viewerUrl) {
                webView.loadUrl(viewerUrl)
            }
        },
        onRelease = { webView ->
            webView.destroy()
        }
    )
}
