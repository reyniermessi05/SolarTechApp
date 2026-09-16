package com.example.solarapp.ui.screens

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

@Composable
fun FaultFinderScreen(
    model: String,
    onNavigateToDetail: (Int, String) -> Unit = { _, _ -> },
    viewModel: FaultViewModel = hiltViewModel()
) {
    LaunchedEffect(model) {
        viewModel.setEquipmentType(model)
    }

    val searchQuery by viewModel.searchQuery.collectAsState()
    val faultCodes by viewModel.faultCodes.collectAsState()

    val configuration = LocalConfiguration.current
    val isSpanish = configuration.locales.get(0)?.language == "es"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(androidx.compose.ui.res.stringResource(com.example.solarapp.R.string.search_fault_codes)) },
            singleLine = true
        )

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
                        .clickable { onNavigateToDetail(fault.id, fault.faultCode) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val title = if (isSpanish && fault.issueTitleEs.isNotEmpty()) fault.issueTitleEs else fault.issueTitle
                        val steps = if (isSpanish && fault.troubleshootingStepsEs.isNotEmpty()) fault.troubleshootingStepsEs else fault.troubleshootingSteps
                        Text(
                            text = "${fault.faultCode}: $title",
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
