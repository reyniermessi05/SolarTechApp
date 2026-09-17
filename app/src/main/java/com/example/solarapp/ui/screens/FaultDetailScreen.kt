package com.example.solarapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solarapp.ui.FaultViewModel

@Composable
fun FaultDetailScreen(
    faultId: Int,
    onNavigateToPdf: (String, Int) -> Unit = { _, _ -> },
    viewModel: FaultViewModel = hiltViewModel()
) {
    val faultCode by remember(faultId) { viewModel.getFaultCodeById(faultId) }.collectAsState(initial = null)

    val configuration = LocalConfiguration.current
    val isSpanish = configuration.locales.get(0)?.language == "es"
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Toast.makeText(context, "AI Assistant (Requiere Conexión) - Próximamente", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val currentFaultCode = faultCode
            if (currentFaultCode != null) {
                val title = if (isSpanish && currentFaultCode.issueTitleEs.isNotEmpty()) currentFaultCode.issueTitleEs else currentFaultCode.issueTitle
                val steps = if (isSpanish && currentFaultCode.troubleshootingStepsEs.isNotEmpty()) currentFaultCode.troubleshootingStepsEs else currentFaultCode.troubleshootingSteps

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.solarapp.R.string.troubleshooting_steps),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LinkableText(
                    text = steps,
                    modifier = Modifier.padding(bottom = 16.dp),
                    onLinkClick = { linkData ->
                        // Link data could be like "manual_hem2.pdf, Pag: 45"
                        val parts = linkData.split(",")
                        val fileName = parts.first().trim()
                        if (fileName.isNotEmpty()) {
                            val pageNumber = if (parts.size > 1) {
                                parts[1].substringAfter(":").trim().toIntOrNull() ?: 1
                            } else 1
                            onNavigateToPdf(fileName, pageNumber)
                        }
                    }
                )
            } else {
                Text(androidx.compose.ui.res.stringResource(com.example.solarapp.R.string.loading))
            }
        }
    }
}

@Composable
fun LinkableText(
    text: String,
    modifier: Modifier = Modifier,
    onLinkClick: (String) -> Unit = {}
) {
    val linkRegex = "\\[Enlace: (.*?)\\]".toRegex()

    val matches = linkRegex.findAll(text).toList()

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        for (match in matches) {
            val linkText = match.groupValues[1]
            append(text.substring(lastIndex, match.range.first))

            pushStringAnnotation(tag = "LINK", annotation = linkText)
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(linkText)
            }
            pop()
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "LINK", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onLinkClick(annotation.item)
                }
        }
    )
}
