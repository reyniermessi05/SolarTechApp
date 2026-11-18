package com.example.solarapp.ui.screens

import androidx.compose.runtime.Composable
import com.example.solarapp.ui.components.EquipmentSelectionMenu

private val LOTO_MODEL_FILES = mapOf(
    "HEM Gen. 2" to "model_hem2.glb",
    "HEM Gen. 3" to "model_hem3.glb",
    "DC/DC Converter Gen. 3" to "model_dcdc3.glb"
)

@Composable
fun LotoMenuScreen(
    onEquipmentSelected: (equipmentName: String, modelFileName: String) -> Unit
) {
    EquipmentSelectionMenu(
        onModelSelected = { equipmentName ->
            val modelFileName = LOTO_MODEL_FILES[equipmentName] ?: "model_hem2.glb"
            onEquipmentSelected(equipmentName, modelFileName)
        }
    )
}
