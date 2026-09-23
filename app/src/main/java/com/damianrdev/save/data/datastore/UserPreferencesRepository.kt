package com.damianrdev.save.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val useCustomTabs: Boolean = true,
    val themeMode: String = "AMOLED_BLACK", // SYSTEM, LIGHT, DARK, AMOLED_BLACK
    val defaultCollectionId: Long? = null,
    val cleanTrackingParams: Boolean = true,
    val autoFetchMetadata: Boolean = true,
    val githubToken: String? = null
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_USE_CUSTOM_TABS = booleanPreferencesKey("use_custom_tabs")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DEFAULT_COLLECTION_ID = longPreferencesKey("default_collection_id")
        val KEY_CLEAN_TRACKING_PARAMS = booleanPreferencesKey("clean_tracking_params")
        val KEY_AUTO_FETCH_METADATA = booleanPreferencesKey("auto_fetch_metadata")
        val KEY_GITHUB_TOKEN = stringPreferencesKey("github_token")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { preferences ->
        val useCustomTabs = preferences[KEY_USE_CUSTOM_TABS] ?: true
        val themeMode = preferences[KEY_THEME_MODE] ?: "AMOLED_BLACK"
        val defaultCollectionId = preferences[KEY_DEFAULT_COLLECTION_ID]
        val cleanTrackingParams = preferences[KEY_CLEAN_TRACKING_PARAMS] ?: true
        val autoFetchMetadata = preferences[KEY_AUTO_FETCH_METADATA] ?: true
        val githubToken = preferences[KEY_GITHUB_TOKEN]

        UserPreferences(
            useCustomTabs = useCustomTabs,
            themeMode = themeMode,
            defaultCollectionId = defaultCollectionId,
            cleanTrackingParams = cleanTrackingParams,
            autoFetchMetadata = autoFetchMetadata,
            githubToken = githubToken
        )
    }

    suspend fun setUseCustomTabs(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_USE_CUSTOM_TABS] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setDefaultCollectionId(collectionId: Long?) {
        dataStore.edit { preferences ->
            if (collectionId != null) {
                preferences[KEY_DEFAULT_COLLECTION_ID] = collectionId
            } else {
                preferences.remove(KEY_DEFAULT_COLLECTION_ID)
            }
        }
    }

    suspend fun setCleanTrackingParams(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_CLEAN_TRACKING_PARAMS] = enabled
        }
    }

    suspend fun setAutoFetchMetadata(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_FETCH_METADATA] = enabled
        }
    }

    suspend fun setGithubToken(token: String?) {
        dataStore.edit { preferences ->
            if (!token.isNullOrBlank()) {
                preferences[KEY_GITHUB_TOKEN] = token.trim()
            } else {
                preferences.remove(KEY_GITHUB_TOKEN)
            }
        }
    }
}
