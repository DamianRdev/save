package com.damianrdev.save.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.damianrdev.save.data.datastore.UserPreferences
import com.damianrdev.save.data.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setUseCustomTabs(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setUseCustomTabs(enabled)
        }
    }

    fun setCleanTrackingParams(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCleanTrackingParams(enabled)
        }
    }

    fun setAutoFetchMetadata(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAutoFetchMetadata(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }
}
