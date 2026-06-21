package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val IS_SETUP_COMPLETED = booleanPreferencesKey("is_setup_completed")
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val FARM_NAME = stringPreferencesKey("farm_name")
    }

    val isSetupCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_SETUP_COMPLETED] ?: false
        }

    val ownerName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[OWNER_NAME] ?: ""
        }

    val farmName: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[FARM_NAME] ?: ""
        }

    suspend fun saveSetupConfiguration(owner: String, farm: String, isCompleted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OWNER_NAME] = owner
            preferences[FARM_NAME] = farm
            preferences[IS_SETUP_COMPLETED] = isCompleted
        }
    }
    
    suspend fun completeSetup() {
        context.dataStore.edit { preferences ->
            preferences[IS_SETUP_COMPLETED] = true
        }
    }
}
