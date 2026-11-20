package com.example.solarapp.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.webkit.WebViewAssetLoader
import com.example.solarapp.R

private enum class LotoStatusColor(val hexColor: ComposeColor) {
    GREEN(ComposeColor(0xFF4ADE80)),
    RED(ComposeColor(0xFFF87171)),
    YELLOW(ComposeColor(0xFFFACC15)),
    PURPLE(ComposeColor(0xFFC084FC))
}

private fun highlightDoorColorPhrases(text: String): AnnotatedString {
    val phraseColors = listOf(
        Regex("""\b(green doors?|puertas? verdes?)\b""", RegexOption.IGNORE_CASE) to LotoStatusColor.GREEN.hexColor,
        Regex("""\b(red doors?|puertas? rojas?)\b""", RegexOption.IGNORE_CASE) to LotoStatusColor.RED.hexColor,
        Regex("""\b(yellow doors?|puertas? amarillas?)\b""", RegexOption.IGNORE_CASE) to LotoStatusColor.YELLOW.hexColor,
        Regex("""\b(purple doors?|puertas? moradas?|puertas? púrpuras?)\b""", RegexOption.IGNORE_CASE) to LotoStatusColor.PURPLE.hexColor
    )
    return buildAnnotatedString {
        append(text)
        phraseColors.forEach { (regex, color) ->
            regex.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    ),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
    }
}

private data class LotoStatusOption(
    val code: String,
    val titleEn: String,
    val titleEs: String,
    val colors: List<LotoStatusColor>,
    val activitiesEn: String,
    val activitiesEs: String
)

private val HEM_GEN2_STATUSES = listOf(
    LotoStatusOption(
        code = "1A",
        titleEn = "1A. Equipment running",
        titleEs = "1A. Equipo en marcha",
        colors = listOf(LotoStatusColor.RED),
        activitiesEn = "Equipment running. Only remote measures by software (PowerCommsV). In case of connecting the ethernet cable, keep a safety distance of 5 m / 16 feet.",
        activitiesEs = "Equipo en marcha. Solo mediciones remotas por software (PowerCommsV). En caso de conectar el cable Ethernet, mantener una distancia de seguridad de 5 m / 16 pies."
    ),
    LotoStatusOption(
        code = "2",
        titleEn = "2. Full LOTOTO",
        titleEs = "2. LOTOTO Completo",
        colors = listOf(LotoStatusColor.GREEN, LotoStatusColor.RED, LotoStatusColor.PURPLE),
        activitiesEn = "Maintenance, repairs, troubleshooting without voltage at green doors.\nPurple Doors: 1. If strings disconnect positive and negative pole: Purple doors are green in night time and day time.",
        activitiesEs = "Mantenimiento, reparaciones y diagnóstico sin tensión en puertas verdes.\nPuertas Moradas: 1. Si los strings desconectan polo positivo y negativo: las puertas moradas son verdes de noche y de día."
    ),
    LotoStatusOption(
        code = "3A",
        titleEn = "3A. Partial AC/DC LOTOTO (With AASS)",
        titleEs = "3A. LOTOTO Parcial AC/DC (Con SSAA)",
        colors = listOf(LotoStatusColor.YELLOW, LotoStatusColor.RED, LotoStatusColor.PURPLE),
        activitiesEn = "Measure or visualization. After applying all the steps, it is mandatory to use ELECTRICAL 1 PPE for measure or visualization at yellow doors. Always wait for fan stop.\nPurple door: Visual inspection and fault identification through EKOR. It is mandatory to use the ELECTRICAL 2 PPE.",
        activitiesEs = "Medición o visualización. Tras aplicar todos los pasos, es obligatorio usar EPP ELÉCTRICO 1 para medición o visualización en puertas amarillas. Esperar siempre a que se detenga el ventilador.\nPuerta morada: Inspección visual e identificación de fallas mediante EKOR. Es obligatorio usar EPP ELÉCTRICO 2."
    ),
    LotoStatusOption(
        code = "3B",
        titleEn = "3B. Partial AC/DC LOTOTO (Without AASS)",
        titleEs = "3B. LOTOTO Parcial AC/DC (Sin SSAA)",
        colors = listOf(LotoStatusColor.GREEN, LotoStatusColor.RED),
        activitiesEn = "Maintenance, repairs, troubleshooting without voltage at green doors.",
        activitiesEs = "Mantenimiento, reparaciones y diagnóstico sin tensión en puertas verdes."
    ),
    LotoStatusOption(
        code = "4",
        titleEn = "4. Partial AC LOTOTO",
        titleEs = "4. LOTOTO Parcial AC",
        colors = listOf(LotoStatusColor.GREEN, LotoStatusColor.RED),
        activitiesEn = "Maintenance, repairs, troubleshooting without voltage at green doors.",
        activitiesEs = "Mantenimiento, reparaciones y diagnóstico sin tensión en puertas verdes."
    ),
    LotoStatusOption(
        code = "5A",
        titleEn = "5A. Partial DC LOTOTO",
        titleEs = "5A. LOTOTO Parcial DC",
        colors = listOf(LotoStatusColor.GREEN, LotoStatusColor.RED, LotoStatusColor.PURPLE),
        activitiesEn = "Maintenance, repairs, troubleshooting without voltage at green doors.\nPurple Doors: 1. If strings disconnect positive and negative pole: Purple doors are green in night time and day time.",
        activitiesEs = "Mantenimiento, reparaciones y diagnóstico sin tensión en puertas verdes.\nPuertas Moradas: 1. Si los strings desconectan polo positivo y negativo: las puertas moradas son verdes de noche y de día."
    ),
    LotoStatusOption(
        code = "6",
        titleEn = "6. Partial DC LOTOTO (With AASS)",
        titleEs = "6. LOTOTO Parcial DC (Con SSAA)",
        colors = listOf(LotoStatusColor.YELLOW, LotoStatusColor.RED),
        activitiesEn = "Measure, visualization and verification of the fans. After applying all the steps, it is mandatory to use ELECTRICAL 1 PPE for measure or visualization at yellow doors. Always wait for fan stop.",
        activitiesEs = "Medición, visualización y verificación de los ventiladores. Tras aplicar todos los pasos, es obligatorio usar EPP ELÉCTRICO 1 para medición o visualización en puertas amarillas. Esperar siempre a que se detenga el ventilador."
    )
)

private fun getGreenStatusesForCabinet(cabinetId: String): List<LotoStatusOption> {
    val greenCodes = when (cabinetId) {
        "1", "2", "6", "7", "24", "25" -> listOf("2", "5A")
        "12", "13", "14", "16" -> listOf("2", "4")
        "3", "4", "5", "8", "9", "10", "11", "15", "17", "18", "19", "20", "21", "22", "23" ->
            listOf("2", "3B", "4", "5A")
        else -> emptyList()
    }
    return HEM_GEN2_STATUSES.filter { it.code in greenCodes }
}

private fun getCompatibleStatusesForCabinets(cabinets: List<SelectedCabinet>): List<LotoStatusOption> {
    if (cabinets.isEmpty()) return emptyList()
    var allowedCodes = getGreenStatusesForCabinet(cabinets.first().cabinetId).map { it.code }.toSet()
    for (i in 1 until cabinets.size) {
        val nextCodes = getGreenStatusesForCabinet(cabinets[i].cabinetId).map { it.code }.toSet()
        allowedCodes = allowedCodes.intersect(nextCodes)
    }
    val canonicalOrder = listOf("2", "3B", "4", "5A")
    return canonicalOrder.mapNotNull { code ->
        if (code in allowedCodes) HEM_GEN2_STATUSES.find { it.code == code } else null
    }
}

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

private fun loadLotoHelpBitmap(context: Context, index: Int): ImageBitmap? {
    val candidates = listOf(
        "loto_help/$index.png",
        "loto_help/$index.jpg",
        "loto_help/$index.jpeg",
        "$index.png",
        "$index.jpg",
        "$index.jpeg"
    )
    for (path in candidates) {
        try {
            context.assets.open(path).use { stream ->
                val bmp = BitmapFactory.decodeStream(stream)
                if (bmp != null) return bmp.asImageBitmap()
            }
        } catch (_: Exception) {
        }
    }
    return null
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Loto3DViewerScreen(
    equipmentName: String,
    modelFileName: String,
    resetViewTrigger: Int = 0,
    helpTrigger: Int = 0,
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
    var selectedCabinetStatus by remember { mutableStateOf<LotoStatusOption?>(null) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var multiSelectedCabinets by remember { mutableStateOf<List<SelectedCabinet>>(emptyList()) }
    var multiSelectedStatus by remember { mutableStateOf<LotoStatusOption?>(null) }
    var infoStatusDialogOption by remember { mutableStateOf<LotoStatusOption?>(null) }
    var showGridDangerDialog by remember { mutableStateOf(false) }
    var activeStatus by remember { mutableStateOf<LotoStatusOption?>(null) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var showVisualHelpGallery by remember { mutableStateOf(false) }
    var lightboxPhotoIndex by remember { mutableStateOf<Int?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastHandledReset by remember { mutableIntStateOf(resetViewTrigger) }
    var lastHandledHelp by remember { mutableIntStateOf(helpTrigger) }

    val syncMultiSelectToWebView: (List<SelectedCabinet>) -> Unit = { list ->
        val jsonArray = list.joinToString(prefix = "[", postfix = "]") {
            "\"${it.cabinetId.replace("\"", "\\\"")}\""
        }
        webViewRef?.evaluateJavascript(
            "if (typeof setMultiSelectedCabinets === 'function') { setMultiSelectedCabinets($jsonArray); }",
            null
        )
    }

    val exitMultiSelectMode: () -> Unit = {
        isMultiSelectMode = false
        multiSelectedCabinets = emptyList()
        multiSelectedStatus = null
        showGridDangerDialog = false
        infoStatusDialogOption = null
        webViewRef?.evaluateJavascript(
            "if (typeof exitMultiSelectMode === 'function') { exitMultiSelectMode(); }",
            null
        )
    }

    val applyStatus: (LotoStatusOption?) -> Unit = { status ->
        showStatusDialog = false
        showGridDangerDialog = false
        infoStatusDialogOption = null
        selectedCabinetStatus = null
        selectedCabinet = null
        isMultiSelectMode = false
        multiSelectedCabinets = emptyList()
        multiSelectedStatus = null
        activeStatus = status
        val code = status?.code ?: ""
        webViewRef?.evaluateJavascript(
            "if (typeof applyLotoStatusColors === 'function') { applyLotoStatusColors('$code'); }",
            null
        )
    }

    LaunchedEffect(resetViewTrigger) {
        if (resetViewTrigger != lastHandledReset) {
            lastHandledReset = resetViewTrigger
            showGridDangerDialog = false
            infoStatusDialogOption = null
            selectedCabinetStatus = null
            selectedCabinet = null
            isMultiSelectMode = false
            multiSelectedCabinets = emptyList()
            multiSelectedStatus = null
            webViewRef?.evaluateJavascript(
                "if (typeof resetToInitialOverview === 'function') { resetToInitialOverview(); }",
                null
            )
        }
    }

    LaunchedEffect(helpTrigger) {
        if (helpTrigger != lastHandledHelp) {
            lastHandledHelp = helpTrigger
            showVisualHelpGallery = true
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
        infoStatusDialogOption = null
        selectedCabinetStatus = null
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
                            infoStatusDialogOption = null
                            if (isMultiSelectMode) {
                                val exists = multiSelectedCabinets.any { it.cabinetId == id }
                                val updated = if (exists) {
                                    multiSelectedCabinets.filterNot { it.cabinetId == id }
                                } else {
                                    multiSelectedCabinets + SelectedCabinet(id, nameEn, nameEs, isGrid)
                                }
                                multiSelectedCabinets = updated
                                val eligibleCodes = getCompatibleStatusesForCabinets(updated).map { it.code }
                                if (multiSelectedStatus?.code !in eligibleCodes) {
                                    multiSelectedStatus = null
                                }
                                syncMultiSelectToWebView(updated)
                            } else {
                                selectedCabinetStatus = null
                                selectedCabinet = SelectedCabinet(id, nameEn, nameEs, isGrid)
                            }
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
                .fillMaxWidth(0.45f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            selectedCabinet?.let { cabinet ->
                val localizedCabinetName = if (isSpanish) cabinet.nameEs else cabinet.nameEn
                val greenStatuses = remember(cabinet.cabinetId) {
                    getGreenStatusesForCabinet(cabinet.cabinetId)
                }
                val isHighVoltageCorner = cabinet.isGridConnection ||
                    cabinet.cabinetId == "12" ||
                    cabinet.cabinetId == "13"
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
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizedCabinetName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighVoltageCorner) {
                                    ComposeColor(0xFFF87171)
                                } else {
                                    ComposeColor(0xFF90CAF9)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = dismissPanel,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.loto_back_button),
                                    tint = ComposeColor.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = stringResource(R.string.loto_select_green_status),
                            style = MaterialTheme.typography.bodySmall,
                            color = ComposeColor(0xFFD1D5DB)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (greenStatuses.isEmpty()) {
                                val emptyMessage = when {
                                    cabinet.isGridConnection || cabinet.cabinetId == "12" ->
                                        stringResource(R.string.loto_empty_grid_status)
                                    cabinet.cabinetId == "2" || cabinet.cabinetId == "25" ->
                                        stringResource(R.string.loto_empty_purple_status)
                                    else ->
                                        stringResource(R.string.loto_empty_generic_status)
                                }
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = if (cabinet.isGridConnection || cabinet.cabinetId == "12") {
                                                ComposeColor(0x88EF5350)
                                            } else {
                                                ComposeColor(0x88C084FC)
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (cabinet.isGridConnection || cabinet.cabinetId == "12") {
                                        ComposeColor(0x26EF4444)
                                    } else {
                                        ComposeColor(0x26C084FC)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (cabinet.isGridConnection || cabinet.cabinetId == "12") {
                                                ComposeColor(0xFFF87171)
                                            } else {
                                                ComposeColor(0xFFC084FC)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = emptyMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ComposeColor(0xFFF3F4F6)
                                        )
                                    }
                                }
                            } else {
                                greenStatuses.forEach { statusOption ->
                                    val isSelected = selectedCabinetStatus?.code == statusOption.code
                                    val statusLabel = if (isSpanish) statusOption.titleEs else statusOption.titleEn
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) ComposeColor(0xFF4ADE80) else ComposeColor(0x44FFFFFF),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                val nextSelection = if (isSelected) null else statusOption
                                                selectedCabinetStatus = nextSelection
                                                webViewRef?.evaluateJavascript(
                                                    "if (typeof tintSelectedCabinetGreen === 'function') { tintSelectedCabinetGreen(${nextSelection != null}); }",
                                                    null
                                                )
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) ComposeColor(0x264ADE80) else ComposeColor(0xFF2A2A2A)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) {
                                                    Icons.Default.CheckCircle
                                                } else {
                                                    Icons.Default.RadioButtonUnchecked
                                                },
                                                contentDescription = null,
                                                tint = if (isSelected) ComposeColor(0xFF4ADE80) else ComposeColor(0xFF9CA3AF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = statusLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = ComposeColor.White,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = { infoStatusDialogOption = statusOption },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Info,
                                                    contentDescription = stringResource(R.string.loto_status_info),
                                                    tint = ComposeColor(0xFF90CAF9),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (cabinet.cabinetId == "2" || cabinet.cabinetId == "6" || cabinet.cabinetId == "25") {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = ComposeColor(0x88C084FC),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        color = ComposeColor(0x26C084FC)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = ComposeColor(0xFFC084FC),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.loto_purple_area_warning),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ComposeColor(0xFFF3F4F6)
                                            )
                                        }
                                    }
                                }
                                if (isHighVoltageCorner) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = ComposeColor(0x88F87171),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        color = ComposeColor(0x26F87171)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = ComposeColor(0xFFF87171),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.loto_grid_high_voltage_warning),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = ComposeColor(0xFFF3F4F6)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    isMultiSelectMode = true
                                    multiSelectedCabinets = listOf(cabinet)
                                    multiSelectedStatus = selectedCabinetStatus
                                    selectedCabinet = null
                                    selectedCabinetStatus = null
                                    showGridDangerDialog = false
                                    infoStatusDialogOption = null
                                    val escapedId = cabinet.cabinetId.replace("'", "\\'")
                                    webViewRef?.evaluateJavascript(
                                        "if (typeof enterMultiSelectMode === 'function') { enterMultiSelectMode('$escapedId'); }",
                                        null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = ComposeColor(0xFF2D3748),
                                    contentColor = ComposeColor(0xFF90CAF9)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SelectAll,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(R.string.loto_multiple_cabinets))
                            }
                            Button(
                                onClick = {
                                    if (isHighVoltageCorner) {
                                        showGridDangerDialog = true
                                    } else if (selectedCabinetStatus != null) {
                                        onApplyLoto(equipmentName, localizedCabinetName)
                                    }
                                },
                                enabled = selectedCabinetStatus != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHighVoltageCorner) ComposeColor(0xFFC62828) else ComposeColor(0xFF1565C0),
                                    contentColor = ComposeColor.White,
                                    disabledContainerColor = ComposeColor(0xFF374151),
                                    disabledContentColor = ComposeColor(0xFF9CA3AF)
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

        @OptIn(ExperimentalLayoutApi::class)
        AnimatedVisibility(
            visible = isMultiSelectMode,
            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxWidth(0.45f)
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            val eligibleStatuses = remember(multiSelectedCabinets) {
                getCompatibleStatusesForCabinets(multiSelectedCabinets)
            }
            val hasPurpleCabinet = multiSelectedCabinets.any {
                it.cabinetId == "2" || it.cabinetId == "6" || it.cabinetId == "25"
            }
            val hasHighVoltageCorner = multiSelectedCabinets.any {
                it.isGridConnection || it.cabinetId == "12" || it.cabinetId == "13"
            }
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
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.loto_multi_cabinets_header, multiSelectedCabinets.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ComposeColor(0xFF4ADE80),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = exitMultiSelectMode,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.loto_cancel_button),
                                tint = ComposeColor.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (multiSelectedCabinets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            multiSelectedCabinets.forEach { cab ->
                                val cabLabel = if (isSpanish) cab.nameEs else cab.nameEn
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ComposeColor(0x264ADE80),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .border(1.dp, ComposeColor(0xFF4ADE80), RoundedCornerShape(999.dp))
                                        .clickable {
                                            val updated = multiSelectedCabinets.filterNot { it.cabinetId == cab.cabinetId }
                                            multiSelectedCabinets = updated
                                            val eligibleCodes = getCompatibleStatusesForCabinets(updated).map { it.code }
                                            if (multiSelectedStatus?.code !in eligibleCodes) {
                                                multiSelectedStatus = null
                                            }
                                            syncMultiSelectToWebView(updated)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = cabLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ComposeColor.White
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = ComposeColor(0xFF4ADE80),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (eligibleStatuses.isEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = ComposeColor(0x88F87171),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = ComposeColor(0x26F87171)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ComposeColor(0xFFF87171),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.loto_multi_no_compatible_status),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ComposeColor(0xFFF3F4F6)
                                    )
                                }
                            }
                        } else {
                            eligibleStatuses.forEach { statusOption ->
                                val isSelected = multiSelectedStatus?.code == statusOption.code
                                val statusLabel = if (isSpanish) statusOption.titleEs else statusOption.titleEn
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) ComposeColor(0xFF4ADE80) else ComposeColor(0x44FFFFFF),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            multiSelectedStatus = if (isSelected) null else statusOption
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ComposeColor(0x264ADE80) else ComposeColor(0xFF2A2A2A)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) {
                                                Icons.Default.CheckCircle
                                            } else {
                                                Icons.Default.RadioButtonUnchecked
                                            },
                                            contentDescription = null,
                                            tint = if (isSelected) ComposeColor(0xFF4ADE80) else ComposeColor(0xFF9CA3AF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = ComposeColor.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { infoStatusDialogOption = statusOption },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Info,
                                                contentDescription = stringResource(R.string.loto_status_info),
                                                tint = ComposeColor(0xFF90CAF9),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (hasPurpleCabinet) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = ComposeColor(0x88C084FC),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = ComposeColor(0x26C084FC)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ComposeColor(0xFFC084FC),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.loto_purple_area_warning),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ComposeColor(0xFFF3F4F6)
                                    )
                                }
                            }
                        }

                        if (hasHighVoltageCorner) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = ComposeColor(0x88F87171),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                color = ComposeColor(0x26F87171)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = ComposeColor(0xFFF87171),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.loto_grid_high_voltage_warning),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ComposeColor(0xFFF3F4F6)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (hasHighVoltageCorner) {
                                    showGridDangerDialog = true
                                } else if (multiSelectedCabinets.isNotEmpty() && multiSelectedStatus != null) {
                                    val joinedNames = multiSelectedCabinets.joinToString(", ") {
                                        if (isSpanish) it.nameEs else it.nameEn
                                    }
                                    onApplyLoto(equipmentName, joinedNames)
                                }
                            },
                            enabled = multiSelectedCabinets.isNotEmpty() && multiSelectedStatus != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasHighVoltageCorner) ComposeColor(0xFFC62828) else ComposeColor(0xFF1565C0),
                                contentColor = ComposeColor.White,
                                disabledContainerColor = ComposeColor(0xFF374151),
                                disabledContentColor = ComposeColor(0xFF9CA3AF)
                            )
                        ) {
                            Text(text = stringResource(R.string.loto_apply_button))
                        }
                        OutlinedButton(
                            onClick = exitMultiSelectMode,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ComposeColor.White
                            )
                        ) {
                            Text(text = stringResource(R.string.loto_cancel_button))
                        }
                    }
                }
            }
        }

        infoStatusDialogOption?.let { infoStatus ->
            val infoTitle = if (isSpanish) infoStatus.titleEs else infoStatus.titleEn
            val infoActivities = if (isSpanish) infoStatus.activitiesEs else infoStatus.activitiesEn
            val annotatedInfoActivities = remember(infoActivities) {
                highlightDoorColorPhrases(infoActivities)
            }
            AlertDialog(
                onDismissRequest = { infoStatusDialogOption = null },
                containerColor = ComposeColor(0xFF1E1E1E),
                titleContentColor = ComposeColor.White,
                textContentColor = ComposeColor.White,
                title = {
                    Text(
                        text = infoTitle,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor(0xFF90CAF9)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.loto_type_of_activities),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = ComposeColor(0xFF9CA3AF)
                        )
                        Text(
                            text = annotatedInfoActivities,
                            style = MaterialTheme.typography.bodySmall,
                            color = ComposeColor(0xFFE5E7EB)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { infoStatusDialogOption = null }) {
                        Text(
                            text = stringResource(R.string.loto_close),
                            color = ComposeColor(0xFF90CAF9)
                        )
                    }
                }
            )
        }

        if (isMultiSelectMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(999.dp),
                color = ComposeColor(0xEE1E1E1E),
                contentColor = ComposeColor.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = ComposeColor(0xFF4ADE80),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = null,
                        tint = ComposeColor(0xFF4ADE80),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.loto_multi_select_banner),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else if (modelFileName == "model_hem2.glb") {
            Surface(
                onClick = { showStatusDialog = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(999.dp),
                color = ComposeColor(0xEE1E1E1E),
                contentColor = ComposeColor.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (activeStatus != null) ComposeColor(0xFF60A5FA) else ComposeColor(0x55FFFFFF),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = ComposeColor(0xFF90CAF9),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (activeStatus != null) {
                            stringResource(R.string.loto_status_chip, activeStatus!!.code)
                        } else {
                            stringResource(R.string.loto_select_status)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = activeStatus != null && selectedCabinet == null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .widthIn(max = 380.dp)
        ) {
            activeStatus?.let { status ->
                val localizedTitle = if (isSpanish) status.titleEs else status.titleEn
                val localizedActivities = if (isSpanish) status.activitiesEs else status.activitiesEn
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ComposeColor(0xEE1E1E1E),
                        contentColor = ComposeColor.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizedTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ComposeColor(0xFF90CAF9),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { applyStatus(null) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.loto_clear_status),
                                    tint = ComposeColor(0xFFBDBDBD),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                status.colors.forEach { colorItem ->
                                    val legendText = when (colorItem) {
                                        LotoStatusColor.GREEN -> stringResource(R.string.loto_legend_green)
                                        LotoStatusColor.RED -> stringResource(R.string.loto_legend_red)
                                        LotoStatusColor.YELLOW -> stringResource(R.string.loto_legend_yellow)
                                        LotoStatusColor.PURPLE -> stringResource(R.string.loto_legend_purple)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(colorItem.hexColor)
                                        )
                                        Text(
                                            text = legendText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ComposeColor(0xFFE5E7EB)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = stringResource(R.string.loto_type_of_activities),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ComposeColor(0xFF9CA3AF)
                            )
                            val annotatedActivities = remember(localizedActivities) {
                                highlightDoorColorPhrases(localizedActivities)
                            }
                            Text(
                                text = annotatedActivities,
                                style = MaterialTheme.typography.bodySmall,
                                color = ComposeColor(0xFFD1D5DB)
                            )
                        }

                        if (status.code != "1A") {
                            Button(
                                onClick = { onApplyLoto(equipmentName, localizedTitle) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ComposeColor(0xFF1565C0),
                                    contentColor = ComposeColor.White
                                )
                            ) {
                                Text(text = stringResource(R.string.loto_apply_button))
                            }
                        }
                    }
                }
            }
        }

        if (showStatusDialog) {
            AlertDialog(
                onDismissRequest = { showStatusDialog = false },
                containerColor = ComposeColor(0xFF1E1E1E),
                titleContentColor = ComposeColor.White,
                textContentColor = ComposeColor.White,
                title = {
                    Text(
                        text = stringResource(R.string.loto_select_status_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HEM_GEN2_STATUSES.forEach { status ->
                            val isSelected = activeStatus?.code == status.code
                            val label = if (isSpanish) status.titleEs else status.titleEn
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { applyStatus(status) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ComposeColor(0xFF1E3A5F) else ComposeColor(0xFF2A2A2A)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = ComposeColor.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        status.colors.forEach { c ->
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(c.hexColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (activeStatus != null) {
                        TextButton(onClick = { applyStatus(null) }) {
                            Text(
                                text = stringResource(R.string.loto_clear_status),
                                color = ComposeColor(0xFF90CAF9)
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStatusDialog = false }) {
                        Text(
                            text = stringResource(R.string.loto_cancel_button),
                            color = ComposeColor(0xFFBDBDBD)
                        )
                    }
                }
            )
        }

        if (showGridDangerDialog && (selectedCabinet != null || (isMultiSelectMode && multiSelectedCabinets.isNotEmpty()))) {
            val targetCabinetLabel = if (isMultiSelectMode) {
                multiSelectedCabinets.joinToString(", ") {
                    if (isSpanish) it.nameEs else it.nameEn
                }
            } else {
                val cabinet = selectedCabinet!!
                if (isSpanish) cabinet.nameEs else cabinet.nameEn
            }
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
                            onApplyLoto(equipmentName, targetCabinetLabel)
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

        if (showVisualHelpGallery) {
            val helpIndices = remember { (1..14).toList() }
            Dialog(
                onDismissRequest = { showVisualHelpGallery = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.80f),
                    shape = RoundedCornerShape(18.dp),
                    color = ComposeColor(0xFF1A1A1A),
                    contentColor = ComposeColor.White
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.loto_visual_help),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = ComposeColor.White
                            )
                            IconButton(onClick = { showVisualHelpGallery = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.loto_close),
                                    tint = ComposeColor.White
                                )
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(helpIndices) { index ->
                                val bitmap = remember(index) { loadLotoHelpBitmap(context, index) }
                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ComposeColor(0xFF2A2A2A))
                                        .border(1.dp, ComposeColor(0x33FFFFFF), RoundedCornerShape(10.dp))
                                        .clickable { lightboxPhotoIndex = index }
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = stringResource(R.string.loto_visual_help_step, index),
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                            .clip(CircleShape)
                                            .background(ComposeColor(0xCC111827))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = index.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ComposeColor.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        lightboxPhotoIndex?.let { currentIndex ->
            Dialog(
                onDismissRequest = { lightboxPhotoIndex = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                var zoomScale by remember(currentIndex) { mutableFloatStateOf(1f) }
                var panX by remember(currentIndex) { mutableFloatStateOf(0f) }
                var panY by remember(currentIndex) { mutableFloatStateOf(0f) }
                val bitmap = remember(currentIndex) { loadLotoHelpBitmap(context, currentIndex) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor(0xF50A0A0A))
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = stringResource(R.string.loto_visual_help_step, currentIndex),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(currentIndex) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (zoomScale > 1.2f) {
                                                zoomScale = 1f
                                                panX = 0f
                                                panY = 0f
                                            } else {
                                                zoomScale = 2.5f
                                            }
                                        }
                                    )
                                }
                                .pointerInput(currentIndex) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val nextScale = (zoomScale * zoom).coerceIn(1f, 4f)
                                        zoomScale = nextScale
                                        if (nextScale > 1f) {
                                            panX += pan.x
                                            panY += pan.y
                                        } else {
                                            panX = 0f
                                            panY = 0f
                                        }
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = zoomScale,
                                    scaleY = zoomScale,
                                    translationX = panX,
                                    translationY = panY
                                )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(ComposeColor(0x99000000))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.loto_visual_help_step, currentIndex),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ComposeColor.White
                        )
                        IconButton(onClick = { lightboxPhotoIndex = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.loto_close),
                                tint = ComposeColor.White
                            )
                        }
                    }

                    if (currentIndex > 1) {
                        IconButton(
                            onClick = { lightboxPhotoIndex = currentIndex - 1 },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(ComposeColor(0x991E1E1E))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = stringResource(R.string.loto_previous_photo),
                                tint = ComposeColor.White
                            )
                        }
                    }

                    if (currentIndex < 14) {
                        IconButton(
                            onClick = { lightboxPhotoIndex = currentIndex + 1 },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(ComposeColor(0x991E1E1E))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = stringResource(R.string.loto_next_photo),
                                tint = ComposeColor.White
                            )
                        }
                    }
                }
            }
        }
    }
}
