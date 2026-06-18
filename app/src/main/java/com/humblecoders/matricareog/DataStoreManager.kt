package com.humblecoders.matricareog

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Local app preferences. Firebase Auth holds the session; we cache login hint for cold start. */
class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth_preferences")

        private val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        private val DISCLAIMER_SHOWN = booleanPreferencesKey("disclaimer_shown")
        private val LOGGED_IN_USER_ID = stringPreferencesKey("logged_in_user_id")
    }

    val termsAccepted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TERMS_ACCEPTED] ?: false
    }

    val disclaimerShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DISCLAIMER_SHOWN] ?: false
    }

    val loggedInUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LOGGED_IN_USER_ID]
    }

    suspend fun getLoggedInUserId(): String? {
        return context.dataStore.data.first()[LOGGED_IN_USER_ID]
    }

    suspend fun saveLoggedInUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[LOGGED_IN_USER_ID] = userId
        }
    }

    suspend fun clearLoggedInUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(LOGGED_IN_USER_ID)
        }
    }

    suspend fun acceptTerms() {
        context.dataStore.edit { preferences ->
            preferences[TERMS_ACCEPTED] = true
        }
    }

    suspend fun resetTerms() {
        context.dataStore.edit { preferences ->
            preferences[TERMS_ACCEPTED] = false
        }
    }

    suspend fun markDisclaimerShown() {
        context.dataStore.edit { preferences ->
            preferences[DISCLAIMER_SHOWN] = true
        }
    }

    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
