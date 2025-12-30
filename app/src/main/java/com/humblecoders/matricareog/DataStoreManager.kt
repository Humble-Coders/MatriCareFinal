package com.humblecoders.matricareog

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("auth_preferences")

        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        private val DISCLAIMER_SHOWN = booleanPreferencesKey("disclaimer_shown")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }

    val termsAccepted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[TERMS_ACCEPTED] ?: false
    }

    val disclaimerShown: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DISCLAIMER_SHOWN] ?: false
    }

    suspend fun saveUserSession(userId: String, email: String, name: String) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_ID] = userId
            preferences[USER_EMAIL] = email
            preferences[USER_NAME] = name
        }
    }

    suspend fun clearUserSession() {
        println("🔵 DataStoreManager: Starting clearUserSession")
        context.dataStore.edit { preferences ->
            // Only clear user session data, preserve terms acceptance
            val termsAcceptedBefore = preferences[TERMS_ACCEPTED]
            println("🔵 DataStoreManager: Terms accepted before clearing: $termsAcceptedBefore")
            
            preferences.remove(IS_LOGGED_IN)
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_NAME)
            // Note: TERMS_ACCEPTED is intentionally preserved
            
            val termsAcceptedAfter = preferences[TERMS_ACCEPTED]
            println("🔵 DataStoreManager: Terms accepted after clearing: $termsAcceptedAfter")
        }
        println("🔵 DataStoreManager: clearUserSession completed")
    }

    suspend fun acceptTerms() {
        println("🔵 DataStoreManager: Starting acceptTerms")
        context.dataStore.edit { preferences ->
            val termsAcceptedBefore = preferences[TERMS_ACCEPTED]
            println("🔵 DataStoreManager: Terms accepted before setting: $termsAcceptedBefore")
            
            preferences[TERMS_ACCEPTED] = true
            println("🔵 DataStoreManager: Set TERMS_ACCEPTED to true")
            
            val termsAcceptedAfter = preferences[TERMS_ACCEPTED]
            println("🔵 DataStoreManager: Terms accepted after setting: $termsAcceptedAfter")
        }
        println("🔵 DataStoreManager: acceptTerms completed")
    }
    
    suspend fun resetTerms() {
        println("🔵 DataStoreManager: Starting resetTerms")
        context.dataStore.edit { preferences ->
            preferences[TERMS_ACCEPTED] = false
            println("🔵 DataStoreManager: Set TERMS_ACCEPTED to false")
        }
        println("🔵 DataStoreManager: resetTerms completed")
    }
    
    suspend fun markDisclaimerShown() {
        context.dataStore.edit { preferences ->
            preferences[DISCLAIMER_SHOWN] = true
        }
    }
    
    suspend fun clearAllData() {
        println("🔵 DataStoreManager: Starting clearAllData")
        context.dataStore.edit { preferences ->
            preferences.clear()
            println("🔵 DataStoreManager: Cleared all preferences")
        }
        println("🔵 DataStoreManager: clearAllData completed")
    }
    
    suspend fun debugPrintAllPreferences() {
        println("🔵 DataStoreManager: Debug - Current preferences:")
        context.dataStore.data.collect { preferences ->
            println("🔵 DataStoreManager: IS_LOGGED_IN: ${preferences[IS_LOGGED_IN]}")
            println("🔵 DataStoreManager: USER_ID: ${preferences[USER_ID]}")
            println("🔵 DataStoreManager: USER_EMAIL: ${preferences[USER_EMAIL]}")
            println("🔵 DataStoreManager: USER_NAME: ${preferences[USER_NAME]}")
            println("🔵 DataStoreManager: TERMS_ACCEPTED: ${preferences[TERMS_ACCEPTED]}")
        }
    }
}