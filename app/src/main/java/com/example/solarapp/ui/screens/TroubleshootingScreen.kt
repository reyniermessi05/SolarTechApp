package com.example.solarapp.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun TroubleshootingScreen(onModelSelected: (String) -> Unit) {
    EquipmentSelectionMenu(onModelSelected = onModelSelected)
}
