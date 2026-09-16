package com.example.solarapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fault_codes")
data class FaultCode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val equipmentType: String,
    val faultCode: String,
    val issueTitle: String,
    val troubleshootingSteps: String,
    val issueTitleEs: String = "",
    val troubleshootingStepsEs: String = ""
)
