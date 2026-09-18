package com.example.solarapp.ui.screens
import androidx.compose.material.icons.filled.Map
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.width

import androidx.compose.material.icons.filled.Clear
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solarapp.ui.FaultViewModel
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalConfiguration
import com.example.solarapp.data.SeedState
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

@Composable
fun FaultFinderScreen(
    model: String,
    onNavigateToDetail: (Int, String) -> Unit = { _, _ -> },
    onNavigateToPdf: (String, Int) -> Unit = { _, _ -> },
    viewModel: FaultViewModel = hiltViewModel()
) {
    LaunchedEffect(model) {
        viewModel.setEquipmentType(model)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val faultCodes by viewModel.faultCodes.collectAsState()
    val seedState by viewModel.seedState.collectAsState()

    val configuration = LocalConfiguration.current
    val isSpanish = configuration.locales.get(0)?.language == "es"
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.weight(1f),
                label = { Text(androidx.compose.ui.res.stringResource(com.example.solarapp.R.string.search_fault_codes)) },
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        androidx.compose.material3.IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            androidx.compose.material3.Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Clear,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            androidx.compose.material3.IconButton(
                onClick = {
                    try {
                        when (model) {
                            "HEM Gen. 2" -> onNavigateToPdf("diagrama_hem2.pdf", 0)
                            "HEM Gen. 3" -> onNavigateToPdf("diagrama_hem3.pdf", 0)
                            "DC/DC Converter Gen. 3" -> onNavigateToPdf("diagrama_dcdc3.pdf", 0)
                            else -> android.widget.Toast.makeText(context, "No schematics for $model yet", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FaultFinderScreen", "Error navigating to PDF", e)
                        android.widget.Toast.makeText(context, "Error opening PDF", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Map,
                    contentDescription = "Planos",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        when (val state = seedState) {
            is SeedState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SeedState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            is SeedState.Success -> {
                if (faultCodes.isEmpty() && searchQuery.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No results found")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp)
                    ) {
                        items(faultCodes) { fault ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { onNavigateToDetail(fault.id, fault.codigo) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    val title = if (isSpanish && fault.tituloEs.isNotEmpty()) fault.tituloEs else fault.tituloEn
                                    val steps = if (isSpanish && fault.pasosEs.isNotEmpty()) fault.pasosEs else fault.pasosEn
                                    Text(
                                        text = "${fault.codigo}: $title",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = steps,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
