package com.example.solarapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FaultDao {
    @Query("SELECT * FROM fault_codes WHERE codigo LIKE '%' || :query || '%' OR tituloEn LIKE '%' || :query || '%' OR tituloEs LIKE '%' || :query || '%'")
    fun searchFaultCodes(query: String): Flow<List<FaultCode>>

    @Query("SELECT * FROM fault_codes WHERE id = :id")
    fun getFaultCodeById(id: Int): Flow<FaultCode?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(faultCodes: List<FaultCode>)

    @Query("SELECT COUNT(*) FROM fault_codes")
    suspend fun getFaultCount(): Int
}
