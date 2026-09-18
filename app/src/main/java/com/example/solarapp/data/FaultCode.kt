package com.example.solarapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fault_codes")
data class FaultCode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equipmentType: String,
    val codigo: String,
    val tituloEn: String,
    val tituloEs: String,
    val pasosEn: String,
    val pasosEs: String,
    val documentoPdf: String,
    val pagina: Int
)
