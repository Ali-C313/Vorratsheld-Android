package com.example.vorratsheld.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// Alte Einstellungen aus den SharedPreferences werden beim ersten Start übernommen
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "vorratsheld_settings")) }
)

// Lokale Einstellungen im DataStore. Die Werte werden beim Start einmal geladen,
// damit AppLock, Sprache und Onboarding sie sofort (ohne Coroutine) lesen können.
object KeyValueStore {
    private var dataStore: DataStore<Preferences>? = null
    private var cache: Preferences = emptyPreferences()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Einmal in App.onCreate aufrufen
    fun init(context: Context) {
        val store = context.applicationContext.dataStore
        dataStore = store
        cache = runBlocking { store.data.first() }
    }

    fun getString(key: String): String? = cache[stringPreferencesKey(key)]

    // Cache sofort ändern, gespeichert wird im Hintergrund
    fun putString(key: String, value: String?) {
        val prefKey = stringPreferencesKey(key)
        cache = cache.toMutablePreferences().apply {
            if (value == null) remove(prefKey) else set(prefKey, value)
        }
        scope.launch {
            dataStore?.edit { prefs ->
                if (value == null) prefs.remove(prefKey) else prefs[prefKey] = value
            }
        }
    }
}
