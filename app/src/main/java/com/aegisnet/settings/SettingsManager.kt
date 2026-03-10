package com.aegisnet.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "aegis_settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val WIFI_ONLY_UPDATES = booleanPreferencesKey("wifi_only_updates")
        // Default 24 hours in milliseconds
        val DEFAULT_UPDATE_INTERVAL = longPreferencesKey("default_update_interval")
        const val DEFAULT_INTERVAL_MS = 24L * 60L * 60L * 1000L
    }

    val wifiOnlyUpdates: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WIFI_ONLY_UPDATES] ?: false
    }

    val defaultUpdateIntervalMs: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_UPDATE_INTERVAL] ?: DEFAULT_INTERVAL_MS
    }

    suspend fun setWifiOnlyUpdates(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WIFI_ONLY_UPDATES] = enabled
        }
    }

    suspend fun setDefaultUpdateInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_UPDATE_INTERVAL] = intervalMs
        }
    }
}
