package com.example.solarapp.ui.screens

import androidx.compose.runtime.Composable
import com.example.solarapp.ui.components.EquipmentSelectionMenu

@Composable
fun TroubleshootingScreen(onModelSelected: (String) -> Unit) {
    EquipmentSelectionMenu(onModelSelected = onModelSelected)
}
