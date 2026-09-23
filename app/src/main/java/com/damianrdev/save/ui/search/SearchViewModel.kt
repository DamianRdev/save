package com.damianrdev.save.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.model.Collection
import com.damianrdev.save.domain.model.SearchFilter
import com.damianrdev.save.domain.model.Tag
import com.damianrdev.save.domain.repository.BookmarkRepository
import com.damianrdev.save.domain.repository.CollectionRepository
import com.damianrdev.save.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val filter: SearchFilter = SearchFilter(),
    val results: List<BookmarkWithDetails> = emptyList(),
    val collections: List<Collection> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val collectionRepository: CollectionRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(SearchFilter())

    val uiState: StateFlow<SearchUiState> = combine(
        _filter.flatMapLatest { bookmarkRepository.search(it) },
        collectionRepository.getAllCollections(),
        tagRepository.getAllTags(),
        _filter
    ) { results, collections, tags, currentFilter ->
        SearchUiState(
            filter = currentFilter,
            results = results,
            collections = collections,
            tags = tags,
            isSearching = currentFilter.query.isNotBlank()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChange(query: String) {
        _filter.update { it.copy(query = query) }
    }

    fun toggleOnlyFavorites() {
        _filter.update { it.copy(onlyFavorites = !it.onlyFavorites) }
    }

    fun toggleOnlyUnread() {
        _filter.update { it.copy(onlyUnread = !it.onlyUnread) }
    }

    fun selectCollection(collectionId: Long?) {
        _filter.update {
            it.copy(collectionId = if (it.collectionId == collectionId) null else collectionId)
        }
    }

    fun selectTag(tagId: Long?) {
        _filter.update {
            it.copy(tagId = if (it.tagId == tagId) null else tagId)
        }
    }

    fun toggleFavorite(id: Long, current: Boolean) {
        viewModelScope.launch {
            bookmarkRepository.setFavorite(id, !current)
        }
    }

    fun clearSearch() {
        _filter.value = SearchFilter()
    }
}
