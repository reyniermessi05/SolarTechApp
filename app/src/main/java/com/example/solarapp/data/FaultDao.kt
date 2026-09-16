package com.example.solarapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FaultDao {
    @Query("SELECT * FROM fault_codes WHERE equipmentType = :equipmentType AND (faultCode LIKE '%' || :query || '%' OR issueTitle LIKE '%' || :query || '%')")
    fun searchFaultCodes(equipmentType: String, query: String): Flow<List<FaultCode>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faultCodes: List<FaultCode>)
}
