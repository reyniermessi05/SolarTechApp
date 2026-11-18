package com.example.solarapp.ui.screens

import androidx.compose.runtime.Composable

private data class LotoEquipmentOption(
    val displayName: String,
    val modelFileName: String
)

private val LOTO_EQUIPMENT_OPTIONS = listOf(
    LotoEquipmentOption(displayName = "HEM Gen. 2", modelFileName = "model_hem2.glb"),
    LotoEquipmentOption(displayName = "HEM Gen. 3", modelFileName = "model_hem3.glb"),
    LotoEquipmentOption(displayName = "DC/DC Converter Gen. 3", modelFileName = "model_dcdc3.glb")
)

@Composable
fun LotoMenuScreen(
    onEquipmentSelected: (equipmentName: String, modelFileName: String) -> Unit
) {
    val optionByName = LOTO_EQUIPMENT_OPTIONS.associateBy { it.displayName }

    EquipmentSelectionMenu(
        models = LOTO_EQUIPMENT_OPTIONS.map { it.displayName },
        onModelSelected = { selectedName ->
            val modelFileName = optionByName[selectedName]?.modelFileName ?: "model_hem2.glb"
            onEquipmentSelected(selectedName, modelFileName)
        }
    )
}
