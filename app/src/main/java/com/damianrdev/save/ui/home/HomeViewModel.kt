package com.damianrdev.save.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.data.datastore.UserPreferencesRepository
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.repository.BookmarkRepository
import com.damianrdev.save.domain.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeFilter {
    ALL, UNCATEGORIZED, FAVORITES
}

data class HomeUiState(
    val bookmarks: List<BookmarkWithDetails> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val activeFilter: HomeFilter = HomeFilter.ALL,
    val selectedCollectionId: Long? = null,
    val selectedBookmarkIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val useCustomTabs: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val collectionRepository: CollectionRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _filterState = MutableStateFlow(HomeFilter.ALL)
    private val _selectedCollectionState = MutableStateFlow<Long?>(null)
    private val _selectedIdsState = MutableStateFlow<Set<Long>>(emptySet())
    private val _isSelectionModeState = MutableStateFlow(false)

    private data class FilterState(
        val filter: HomeFilter,
        val selectedCollectionId: Long?,
        val selectedIds: Set<Long>,
        val isSelectionMode: Boolean
    )

    private val _filterParams = combine(
        _filterState,
        _selectedCollectionState,
        _selectedIdsState,
        _isSelectionModeState
    ) { filter, colId, ids, isSel ->
        FilterState(filter, colId, ids, isSel)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        bookmarkRepository.getAllActiveBookmarks(),
        collectionRepository.getAllCollections(),
        _filterParams,
        userPreferencesRepository.userPreferencesFlow
    ) { allBookmarks, collections, params, prefs ->
        val filtered = allBookmarks.filter { item ->
            val b = item.bookmark
            val matchesTab = when (params.filter) {
                HomeFilter.ALL -> true
                HomeFilter.UNCATEGORIZED -> b.collectionId == null
                HomeFilter.FAVORITES -> b.isFavorite
            }
            val matchesCollection = if (params.selectedCollectionId != null) {
                b.collectionId == params.selectedCollectionId
            } else true

            matchesTab && matchesCollection
        }

        HomeUiState(
            bookmarks = filtered,
            collections = collections,
            activeFilter = params.filter,
            selectedCollectionId = params.selectedCollectionId,
            selectedBookmarkIds = params.selectedIds,
            isSelectionMode = params.isSelectionMode,
            useCustomTabs = prefs.useCustomTabs,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun setFilter(filter: HomeFilter) {
        _filterState.value = filter
    }

    fun selectCollection(collectionId: Long?) {
        _selectedCollectionState.value = collectionId
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch {
            bookmarkRepository.setFavorite(id, !current)
        }
    }

    fun toggleSelectBookmark(id: Long) {
        _selectedIdsState.update { current ->
            val updated = if (current.contains(id)) current - id else current + id
            if (updated.isEmpty()) {
                _isSelectionModeState.value = false
            }
            updated
        }
    }

    fun startSelectionMode(initialId: Long) {
        _isSelectionModeState.value = true
        _selectedIdsState.value = setOf(initialId)
    }

    fun clearSelection() {
        _isSelectionModeState.value = false
        _selectedIdsState.value = emptySet()
    }

    fun selectAll() {
        val allIds = uiState.value.bookmarks.map { it.bookmark.id }.toSet()
        _selectedIdsState.value = allIds
    }

    fun deleteSelected() {
        val ids = _selectedIdsState.value.toList()
        viewModelScope.launch {
            bookmarkRepository.moveToTrash(ids)
            clearSelection()
        }
    }

    fun archiveSelected() {
        val ids = _selectedIdsState.value.toList()
        viewModelScope.launch {
            bookmarkRepository.setArchived(ids, true)
            clearSelection()
        }
    }

    fun moveSelectedToCollection(collectionId: Long?) {
        val ids = _selectedIdsState.value.toList()
        viewModelScope.launch {
            bookmarkRepository.moveToCollection(ids, collectionId)
            clearSelection()
        }
    }
}
