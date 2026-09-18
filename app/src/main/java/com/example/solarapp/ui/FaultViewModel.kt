package com.example.solarapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarapp.data.FaultCode
import com.example.solarapp.data.FaultDataSeeder
import com.example.solarapp.repository.FaultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FaultViewModel @Inject constructor(
    private val faultRepository: FaultRepository,
    private val seeder: FaultDataSeeder
) : ViewModel() {

    val seedState = seeder.seedState

    init {
        viewModelScope.launch {
            seeder.seedDatabaseIfNeeded()
        }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _equipmentType = MutableStateFlow("")
    
    fun setEquipmentType(type: String) {
        _equipmentType.value = type
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val faultCodes: StateFlow<List<FaultCode>> = combine(_equipmentType, _searchQuery) { equipmentType, query ->
            Pair(equipmentType, query)
        }
        .flatMapLatest { (equipmentType, query) ->
            faultRepository.searchFaultCodes(equipmentType, query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getFaultCodeById(id: Int): kotlinx.coroutines.flow.Flow<FaultCode?> {
        return faultRepository.getFaultCodeById(id)
    }
}
