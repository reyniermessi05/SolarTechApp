package com.example.solarapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TroubleshootingScreen(onModelSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val models = listOf("HEM Gen. 2", "HEM Gen. 3", "DC/DC Converter Gen. 3")
        models.forEach { model ->
            Button(
                onClick = { onModelSelected(model) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = model, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
