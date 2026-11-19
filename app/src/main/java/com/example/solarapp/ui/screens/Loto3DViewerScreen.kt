package com.example.solarapp.ui.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.example.solarapp.R

private data class SelectedCabinet(
    val cabinetId: String,
    val nameEn: String,
    val nameEs: String,
    val isGridConnection: Boolean
)

private class LotoAndroidBridge(
    private val onSelected: (cabinetId: String, nameEn: String, nameEs: String, isGridConnection: Boolean) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onCabinetSelected(cabinetId: String, nameEn: String, nameEs: String, isGridConnection: Boolean) {
        mainHandler.post {
            onSelected(cabinetId, nameEn, nameEs, isGridConnection)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Loto3DViewerScreen(
    equipmentName: String,
    modelFileName: String,
    resetViewTrigger: Int = 0,
    onApplyLoto: (equipmentName: String, cabinetName: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val appLocales = AppCompatDelegate.getApplicationLocales()
    val isSpanish = remember(appLocales, configuration) {
        val lang = if (!appLocales.isEmpty) {
            appLocales.get(0)?.language
        } else {
            configuration.locales.get(0)?.language
        }
        lang == "es"
    }

    var selectedCabinet by remember { mutableStateOf<SelectedCabinet?>(null) }
    var showGridDangerDialog by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastHandledReset by remember { mutableIntStateOf(resetViewTrigger) }

    LaunchedEffect(resetViewTrigger) {
        if (resetViewTrigger != lastHandledReset) {
            lastHandledReset = resetViewTrigger
            showGridDangerDialog = false
            selectedCabinet = null
            webViewRef?.evaluateJavascript(
                "if (typeof resetToInitialOverview === 'function') { resetToInitialOverview(); }",
                null
            )
        }
    }

    val assetLoader = remember(context) {
        WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
            .build()
    }
    val viewerUrl = remember(modelFileName) {
        "https://appassets.androidplatform.net/assets/loto_viewer.html?model=${Uri.encode(modelFileName)}"
    }

    val dismissPanel: () -> Unit = {
        showGridDangerDialog = false
        selectedCabinet = null
        webViewRef?.evaluateJavascript(
            "if (typeof resetCameraView === 'function') { resetCameraView(); }",
            null
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
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

                    addJavascriptInterface(
                        LotoAndroidBridge { id, nameEn, nameEs, isGrid ->
                            showGridDangerDialog = false
                            selectedCabinet = SelectedCabinet(id, nameEn, nameEs, isGrid)
                        },
                        "AndroidBridge"
                    )

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

                    webViewRef = this
                    loadUrl(viewerUrl)
                }
            },
            update = { webView ->
                webViewRef = webView
                if (webView.url != viewerUrl) {
                    webView.loadUrl(viewerUrl)
                }
            },
            onRelease = { webView ->
                webView.removeJavascriptInterface("AndroidBridge")
                webView.destroy()
            }
        )

        AnimatedVisibility(
            visible = selectedCabinet != null,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.44f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            selectedCabinet?.let { cabinet ->
                val localizedCabinetName = if (isSpanish) cabinet.nameEs else cabinet.nameEn
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ComposeColor(0xEE1E1E1E),
                        contentColor = ComposeColor.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = dismissPanel) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.loto_back_button),
                                        tint = ComposeColor.White
                                    )
                                }
                            }
                            Text(
                                text = localizedCabinetName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (cabinet.isGridConnection) ComposeColor(0xFFEF5350) else ComposeColor(0xFF90CAF9)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.loto_apply_question),
                                style = MaterialTheme.typography.bodyLarge,
                                color = ComposeColor(0xFFE0E0E0)
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (cabinet.isGridConnection || cabinet.cabinetId == "12") {
                                        showGridDangerDialog = true
                                    } else {
                                        onApplyLoto(equipmentName, localizedCabinetName)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (cabinet.isGridConnection) ComposeColor(0xFFC62828) else ComposeColor(0xFF1565C0),
                                    contentColor = ComposeColor.White
                                )
                            ) {
                                Text(text = stringResource(R.string.loto_apply_button))
                            }
                            OutlinedButton(
                                onClick = dismissPanel,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ComposeColor.White
                                )
                            ) {
                                Text(text = stringResource(R.string.loto_back_button))
                            }
                        }
                    }
                }
            }
        }

        if (showGridDangerDialog && selectedCabinet != null) {
            val cabinet = selectedCabinet!!
            val localizedCabinetName = if (isSpanish) cabinet.nameEs else cabinet.nameEn
            AlertDialog(
                onDismissRequest = { showGridDangerDialog = false },
                containerColor = ComposeColor(0xFF1E1E1E),
                titleContentColor = ComposeColor(0xFFEF4444),
                textContentColor = ComposeColor.White,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.loto_grid_danger_title),
                        tint = ComposeColor(0xFFEF4444)
                    )
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.loto_grid_danger_title),
                            fontWeight = FontWeight.ExtraBold,
                            color = ComposeColor(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                },
                text = {
                    Text(
                        text = stringResource(R.string.loto_grid_danger_message),
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showGridDangerDialog = false
                            onApplyLoto(equipmentName, localizedCabinetName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(0xFFDC2626),
                            contentColor = ComposeColor.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.loto_continue_button),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGridDangerDialog = false }) {
                        Text(
                            text = stringResource(R.string.loto_cancel_button),
                            color = ComposeColor(0xFFBDBDBD)
                        )
                    }
                }
            )
        }
    }
}
