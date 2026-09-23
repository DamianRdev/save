package com.damianrdev.save.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.domain.model.Bookmark
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

data class EditBookmarkUiState(
    val bookmarkId: Long = 0L,
    val originalUrl: String = "",
    val title: String = "",
    val description: String = "",
    val note: String = "",
    val selectedCollectionId: Long? = null,
    val tagsInput: String = "",
    val availableCollections: List<Collection> = emptyList(),
    val isSaved: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class EditBookmarkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkRepository: BookmarkRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val bookmarkId: Long = checkNotNull(savedStateHandle["bookmarkId"])
    private var originalBookmark: Bookmark? = null

    private val _uiState = MutableStateFlow(EditBookmarkUiState(bookmarkId = bookmarkId))
    val uiState: StateFlow<EditBookmarkUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val collections = collectionRepository.getAllCollections().first()
            val itemWithDetails = bookmarkRepository.getBookmarkById(bookmarkId).first()

            if (itemWithDetails != null) {
                originalBookmark = itemWithDetails.bookmark
                val b = itemWithDetails.bookmark
                val tagsString = itemWithDetails.tags.joinToString(", ") { it.name }

                _uiState.update {
                    it.copy(
                        originalUrl = b.originalUrl,
                        title = b.title,
                        description = b.description ?: "",
                        note = b.note ?: "",
                        selectedCollectionId = b.collectionId,
                        tagsInput = tagsString,
                        availableCollections = collections,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onTitleChange(title: String) = _uiState.update { it.copy(title = title) }
    fun onDescriptionChange(desc: String) = _uiState.update { it.copy(description = desc) }
    fun onNoteChange(note: String) = _uiState.update { it.copy(note = note) }
    fun onCollectionSelect(id: Long?) = _uiState.update { it.copy(selectedCollectionId = id) }
    fun onTagsChange(tags: String) = _uiState.update { it.copy(tagsInput = tags) }

    fun saveChanges() {
        val orig = originalBookmark ?: return
        val current = _uiState.value

        viewModelScope.launch {
            val updatedBookmark = orig.copy(
                title = current.title.trim(),
                description = current.description.ifBlank { null },
                note = current.note.ifBlank { null },
                collectionId = current.selectedCollectionId,
                updatedAt = System.currentTimeMillis()
            )

            val tagList = current.tagsInput.split(",", " ")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }

            bookmarkRepository.updateBookmark(updatedBookmark, tagList)
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
