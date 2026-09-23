package com.damianrdev.save.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.domain.model.BookmarkWithDetails
import com.damianrdev.save.domain.repository.BookmarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TrashTab {
    TRASH, ARCHIVED
}

data class TrashArchivedUiState(
    val activeTab: TrashTab = TrashTab.TRASH,
    val trashItems: List<BookmarkWithDetails> = emptyList(),
    val archivedItems: List<BookmarkWithDetails> = emptyList()
)

@HiltViewModel
class TrashArchivedViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _tabState = MutableStateFlow(TrashTab.TRASH)

    val uiState: StateFlow<TrashArchivedUiState> = combine(
        _tabState,
        bookmarkRepository.getTrash(),
        bookmarkRepository.getArchived()
    ) { tab, trash, archived ->
        TrashArchivedUiState(
            activeTab = tab,
            trashItems = trash,
            archivedItems = archived
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrashArchivedUiState()
    )

    fun setTab(tab: TrashTab) {
        _tabState.value = tab
    }

    fun restoreItem(id: Long) {
        viewModelScope.launch {
            bookmarkRepository.restoreFromTrash(listOf(id))
        }
    }

    fun unarchiveItem(id: Long) {
        viewModelScope.launch {
            bookmarkRepository.setArchived(listOf(id), false)
        }
    }

    fun deletePermanently(id: Long) {
        viewModelScope.launch {
            bookmarkRepository.deletePermanently(listOf(id))
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            bookmarkRepository.emptyTrash()
        }
    }
}
