package com.damianrdev.save.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val bookmarkId: Long = checkNotNull(savedStateHandle["bookmarkId"])

    val item: StateFlow<BookmarkWithDetails?> = bookmarkRepository.getBookmarkById(bookmarkId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val bookmark: StateFlow<BookmarkWithDetails?> = item

    fun toggleFavorite(current: Boolean) {
        viewModelScope.launch {
            bookmarkRepository.setFavorite(bookmarkId, !current)
        }
    }

    fun toggleRead(current: Boolean = item.value?.bookmark?.isRead ?: false) {
        viewModelScope.launch {
            bookmarkRepository.setRead(bookmarkId, !current)
        }
    }

    fun updateReadingProgress(progress: Float) {
        viewModelScope.launch {
            bookmarkRepository.updateReadingProgress(bookmarkId, progress)
        }
    }

    fun toggleOfflineDownload() {
        val currentStatus = item.value?.bookmark?.offlineStatus
        viewModelScope.launch {
            if (currentStatus == "AVAILABLE") {
                bookmarkRepository.removeOfflineArticle(bookmarkId)
            } else {
                bookmarkRepository.downloadOfflineArticle(bookmarkId)
            }
        }
    }

    fun archive(isArchived: Boolean) {
        viewModelScope.launch {
            bookmarkRepository.setArchived(listOf(bookmarkId), isArchived)
        }
    }

    fun moveToTrash() {
        viewModelScope.launch {
            bookmarkRepository.moveToTrash(listOf(bookmarkId))
        }
    }

    fun refreshMetadata() {
        viewModelScope.launch {
            bookmarkRepository.refreshMetadata(bookmarkId)
        }
    }
}
