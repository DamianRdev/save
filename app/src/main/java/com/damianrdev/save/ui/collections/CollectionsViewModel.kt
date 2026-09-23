package com.damianrdev.save.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionsUiState(
    val collections: List<Collection> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    val uiState: StateFlow<CollectionsUiState> = collectionRepository.getAllCollections()
        .map { CollectionsUiState(collections = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CollectionsUiState(isLoading = true)
        )

    fun createCollection(name: String, colorHex: String = "#6366F1") {
        if (name.isBlank()) return
        viewModelScope.launch {
            collectionRepository.createCollection(name = name.trim(), colorHex = colorHex)
        }
    }

    fun deleteCollection(collection: Collection) {
        viewModelScope.launch {
            collectionRepository.deleteCollection(collection)
        }
    }
}
