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
import androidx.compose.material3.Button
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
import com.example.solarapp.R
import androidx.compose.ui.res.stringResource

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
                val title = if (isSpanish && currentFaultCode.tituloEs.isNotEmpty()) currentFaultCode.tituloEs else currentFaultCode.tituloEn
                val steps = if (isSpanish && currentFaultCode.pasosEs.isNotEmpty()) currentFaultCode.pasosEs else currentFaultCode.pasosEn

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.troubleshooting_steps),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = steps,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (currentFaultCode.documentoPdf.isNotEmpty()) {
                    Button(
                        onClick = {
                            onNavigateToPdf(currentFaultCode.documentoPdf, currentFaultCode.pagina)
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(stringResource(R.string.button_details))
                    }

                    Button(
                        onClick = {
                            onNavigateToPdf("diagrama_hem2.pdf", 1)
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(stringResource(R.string.button_schematic))
                    }
                }
            } else {
                Text(stringResource(R.string.loading))
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
