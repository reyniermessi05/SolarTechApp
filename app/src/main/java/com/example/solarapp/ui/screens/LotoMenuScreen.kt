package com.example.solarapp.ui.screens

import androidx.compose.runtime.Composable

private val LOTO_EQUIPMENT_MODELS = linkedMapOf(
    "HEM Gen. 2" to "model_hem2.glb",
    "HEM Gen. 3" to "model_hem3.glb",
    "DC/DC Converter Gen. 3" to "model_dcdc3.glb"
)

@Composable
fun LotoMenuScreen(
    onEquipmentSelected: (equipmentName: String, modelFileName: String) -> Unit
) {
    EquipmentSelectionMenu(
        models = LOTO_EQUIPMENT_MODELS.keys.toList(),
        onModelSelected = { equipmentName ->
            val modelFileName = LOTO_EQUIPMENT_MODELS[equipmentName] ?: "model_hem2.glb"
            onEquipmentSelected(equipmentName, modelFileName)
        }
    )
}
