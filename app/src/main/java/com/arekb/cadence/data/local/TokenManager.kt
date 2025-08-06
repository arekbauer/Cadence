package com.arekb.cadence.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Datastore instance with "auth_prefs" being the file name
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    suspend fun saveAccessToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = token
        }
    }

    suspend fun getAccessToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[KEY_ACCESS_TOKEN]
        }.first() // .first() gets the first emission from the Flow
    }

    suspend fun saveRefreshToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_REFRESH_TOKEN] = token
        }
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { preferences ->
            preferences[KEY_REFRESH_TOKEN]
        }.first()
    }

    suspend fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    suspend fun clearTokens() {
        dataStore.edit { it.clear() }
    }
}