package com.example.solarapp.repository

import com.example.solarapp.data.FaultCode
import com.example.solarapp.data.FaultDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FaultRepository @Inject constructor(
    private val faultDao: FaultDao
) {
    fun searchFaultCodes(query: String): Flow<List<FaultCode>> {
        return faultDao.searchFaultCodes(query)
    }

    fun getFaultCodeById(id: Int): Flow<FaultCode?> {
        return faultDao.getFaultCodeById(id)
    }
}
