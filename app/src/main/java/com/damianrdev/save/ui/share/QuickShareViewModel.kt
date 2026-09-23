package com.damianrdev.save.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.core.common.NetworkObserver
import com.damianrdev.save.core.common.SmartCategorizer
import com.damianrdev.save.core.common.UrlSanitizer
import com.damianrdev.save.data.datastore.UserPreferencesRepository
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.repository.BookmarkRepository
import com.damianrdev.save.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickShareUiState(
    val rawText: String = "",
    val extractedUrl: String? = null,
    val domain: String = "",
    val title: String = "",
    val note: String = "",
    val selectedCollectionId: Long? = null,
    val suggestedCategoryName: String = "",
    val suggestedCategoryColor: String = "#3B82F6",
    val suggestedTypeBadge: String = "🌐 Enlace",
    val smartSummary: String = "",
    val isOffline: Boolean = false,
    val availableCollections: List<Collection> = emptyList(),
    val tagsInput: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class QuickShareViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val collectionRepository: CollectionRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val networkObserver: NetworkObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickShareUiState())
    val uiState: StateFlow<QuickShareUiState> = _uiState.asStateFlow()

    init {
        loadCollections()
    }

    private fun loadCollections() {
        viewModelScope.launch {
            collectionRepository.getAllCollections().collect { collections ->
                _uiState.update { it.copy(availableCollections = collections) }
            }
        }
    }

    fun processSharedText(text: String?) {
        if (text.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "No se recibió texto para guardar") }
            return
        }

        val url = UrlSanitizer.extractUrl(text)
        if (url == null) {
            _uiState.update { it.copy(errorMessage = "No se detectó un enlace web válido en el texto compartido") }
            return
        }

        val domain = UrlSanitizer.extractDomain(url)
        val inference = SmartCategorizer.inferCategory(url, domain)
        val smartTitle = SmartCategorizer.generateSmartTitle(url, domain, text)
        val smartSummary = SmartCategorizer.generateSmartDescription(url, domain, inference)
        val isOffline = !networkObserver.isOnline

        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            _uiState.update {
                it.copy(
                    rawText = text,
                    extractedUrl = url,
                    domain = domain,
                    title = smartTitle,
                    suggestedCategoryName = inference.name,
                    suggestedCategoryColor = inference.colorHex,
                    suggestedTypeBadge = inference.typeBadge,
                    smartSummary = smartSummary,
                    isOffline = isOffline,
                    selectedCollectionId = prefs.defaultCollectionId
                )
            }
        }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onTagsChange(tags: String) {
        _uiState.update { it.copy(tagsInput = tags) }
    }

    fun onCollectionSelect(collectionId: Long?) {
        _uiState.update { it.copy(selectedCollectionId = collectionId) }
    }

    fun saveBookmark(quickSave: Boolean = false) {
        val currentState = _uiState.value
        val url = currentState.extractedUrl ?: return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val tags = if (quickSave) emptyList() else {
                currentState.tagsInput.split(",", " ")
                    .map { it.trim().removePrefix("#") }
                    .filter { it.isNotBlank() }
            }

            bookmarkRepository.saveBookmark(
                originalUrl = url,
                title = currentState.title.ifBlank { null },
                note = if (quickSave) null else currentState.note.ifBlank { null },
                collectionId = currentState.selectedCollectionId,
                tagNames = tags
            )

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
