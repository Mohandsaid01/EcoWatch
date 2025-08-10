package com.example.ecowatch.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowatch.data.model.Species
import com.example.ecowatch.data.repository.SpeciesRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortBy { NAME, CREATED_AT, TEMP_MAX }

@OptIn(FlowPreview::class)
class SpeciesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SpeciesRepository.get(app)

    private val query = MutableStateFlow("")
    private val sortBy = MutableStateFlow(SortBy.NAME)

    private val source: Flow<List<Species>> =
        query
            .debounce(250)
            .flatMapLatest { q ->
                if (q.isBlank()) repo.allSpecies else repo.searchByName(q)
            }

    val species: StateFlow<List<Species>> =
        combine(source, sortBy) { list, sort ->
            when (sort) {
                SortBy.NAME -> list.sortedBy { it.name.lowercase() }
                SortBy.CREATED_AT -> list.sortedByDescending { it.createdAt ?: 0L }
                SortBy.TEMP_MAX -> list.sortedByDescending { it.maxTemp ?: Double.NEGATIVE_INFINITY }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onSearchChange(newQuery: String) { query.value = newQuery }
    fun onSortChange(newSort: SortBy) { sortBy.value = newSort }

    fun addOrUpdateSpecies(item: Species) = viewModelScope.launch {
        repo.save(item)
    }

    fun deleteById(id: Long) = viewModelScope.launch {
        repo.deleteById(id)
    }

    fun clearAll() = viewModelScope.launch { repo.deleteAll() }

    fun getById(id: Long): Flow<Species?> = repo.getById(id)

    fun evaluateThresholds(species: Species, currentTemp: Double?, currentHum: Double?): List<String> {
        return repo.checkThresholds(species, currentTemp, currentHum)
    }

    fun backupAll() = viewModelScope.launch {
        species.value.forEach { repo.syncUp(it) }
    }

    fun restoreAllReplace() = viewModelScope.launch {
        repo.syncDownReplace()
    }
}
