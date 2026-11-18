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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader

private const val LOTO_VIEWER_BASE_URL = "https://appassets.androidplatform.net/assets/loto_viewer.html"
private const val VIEWER_BACKGROUND_HEX = "#121212"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Loto3DViewerScreen(
    equipmentName: String,
    modelFileName: String
) {
    val context = LocalContext.current

    val assetLoader = remember(context) {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }

    val viewerUrl = remember(equipmentName, modelFileName) {
        Uri.parse(LOTO_VIEWER_BASE_URL)
            .buildUpon()
            .appendQueryParameter("model", modelFileName)
            .appendQueryParameter("name", equipmentName)
            .build()
            .toString()
    }

    val webView = remember(context) {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor(VIEWER_BACKGROUND_HEX))
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = false
                allowContentAccess = false
                setSupportZoom(false)
                builtInZoomControls = false
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                mediaPlaybackRequiresUserGesture = false
            }

            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val response = assetLoader.shouldInterceptRequest(request.url) ?: return null
                    val path = request.url.path.orEmpty().lowercase()
                    when {
                        path.endsWith(".js") || path.endsWith(".mjs") -> {
                            response.mimeType = "application/javascript"
                        }
                        path.endsWith(".glb") -> {
                            response.mimeType = "model/gltf-binary"
                        }
                        path.endsWith(".gltf") -> {
                            response.mimeType = "model/gltf+json"
                        }
                        path.endsWith(".html") -> {
                            response.mimeType = "text/html"
                        }
                    }
                    return response
                }
            }
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.clearHistory()
            webView.removeAllViews()
            webView.destroy()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor(0xFF121212))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webView },
            update = { view ->
                if (view.url != viewerUrl) {
                    view.loadUrl(viewerUrl)
                }
            }
        )
    }
}
