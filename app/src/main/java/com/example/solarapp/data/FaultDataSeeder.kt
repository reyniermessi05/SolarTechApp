package com.example.solarapp.data

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

sealed class SeedState {
    object Loading : SeedState()
    object Success : SeedState()
    data class Error(val message: String) : SeedState()
}

@Singleton
class FaultDataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val faultDao: FaultDao
) {
    private val _seedState = MutableStateFlow<SeedState>(SeedState.Loading)
    val seedState: StateFlow<SeedState> = _seedState.asStateFlow()

    suspend fun seedDatabaseIfNeeded() {
        withContext(Dispatchers.IO) {
            try {
                val count = faultDao.getFaultCount()
                if (count > 0) {
                    _seedState.value = SeedState.Success
                    return@withContext
                }

                val faultsList = mutableListOf<FaultCode>()
                context.assets.open("faults_hem2.csv").use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        val records = CSVFormat.EXCEL.builder()
                            .setHeader()
                            .setSkipHeaderRecord(true)
                            .setIgnoreSurroundingSpaces(true)
                            .build()
                            .parse(reader)

                        for (record in records) {
                            try {
                                faultsList.add(
                                    FaultCode(
                                        codigo = record.get(0).trim(),
                                        tituloEn = record.get(1).trim(),
                                        tituloEs = record.get(2).trim(),
                                        pasosEn = record.get(3).replace("\\n", "\n").trim(),
                                        pasosEs = record.get(4).replace("\\n", "\n").trim(),
                                        documentoPdf = record.get(5).trim(),
                                        pagina = record.get(6).trim().toIntOrNull() ?: 1
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("FaultDataSeeder", "Failed to parse CSV record: $record", e)
                            }
                        }
                    }
                }

                if (faultsList.isNotEmpty()) {
                    faultDao.insertAll(faultsList)
                    _seedState.value = SeedState.Success
                } else {
                    _seedState.value = SeedState.Error("CSV parsed successfully but no valid records were found.")
                }
            } catch (e: Exception) {
                Log.e("FaultDataSeeder", "Critical error seeding database", e)
                _seedState.value = SeedState.Error("Error al cargar datos: ${e.localizedMessage}")
            }
        }
    }
}