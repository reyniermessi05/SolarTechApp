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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.example.solarapp.R

private enum class LotoStatusColor(val hexColor: ComposeColor) {
    GREEN(ComposeColor(0xFF4ADE80)),
    RED(ComposeColor(0xFFF87171)),
    YELLOW(ComposeColor(0xFFFACC15)),
    PURPLE(ComposeColor(0xFFC084FC))
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
    var activeStatus by remember { mutableStateOf<LotoStatusOption?>(null) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastHandledReset by remember { mutableIntStateOf(resetViewTrigger) }

    val applyStatus: (LotoStatusOption?) -> Unit = { status ->
        showStatusDialog = false
        showGridDangerDialog = false
        selectedCabinet = null
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

        if (modelFileName == "model_hem2.glb") {
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
                            activeStatus!!.code
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
                        modifier = Modifier
                            .padding(14.dp)
                            .heightIn(max = 310.dp)
                            .verticalScroll(rememberScrollState()),
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
                        Text(
                            text = localizedActivities,
                            style = MaterialTheme.typography.bodySmall,
                            color = ComposeColor(0xFFD1D5DB)
                        )

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
