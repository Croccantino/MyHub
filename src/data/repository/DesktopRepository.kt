package com.myhub.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.myhub.app.data.model.DesktopServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import java.util.UUID

private val Context.desktopDataStore: DataStore<Preferences> by preferencesDataStore(name = "myhub_desktops")

class DesktopRepository private constructor(private val context: Context) {

    companion object {
        private val KEY_DESKTOPS = stringPreferencesKey("desktops_json")
        private val json = Json { ignoreUnknownKeys = true }

        @Volatile
        private var INSTANCE: DesktopRepository? = null

        fun getInstance(context: Context): DesktopRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DesktopRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val desktopsFlow: Flow<List<DesktopServer>> = context.desktopDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val raw = prefs[KEY_DESKTOPS] ?: return@map emptyList()
            runCatching { json.decodeFromString<List<DesktopServer>>(raw) }.getOrDefault(emptyList())
        }

    /** The first desktop in the sorted list, or null if none. */
    val activeServerFlow: Flow<DesktopServer?> = desktopsFlow.map { it.firstOrNull() }

    suspend fun addDesktop(server: DesktopServer) {
        updateList { list ->
            val newOrder = (list.maxOfOrNull { it.order } ?: -1) + 1
            list + server.copy(
                id = if (server.id.isBlank()) UUID.randomUUID().toString() else server.id,
                order = newOrder
            )
        }
    }

    suspend fun updateDesktop(updated: DesktopServer) {
        updateList { list -> list.map { if (it.id == updated.id) updated else it } }
    }

    suspend fun removeDesktop(id: String) {
        updateList { list -> list.filter { it.id != id } }
    }

    suspend fun moveDesktop(fromIndex: Int, toIndex: Int) {
        updateList { list ->
            val mutable = list.toMutableList()
            val item = mutable.removeAt(fromIndex)
            mutable.add(toIndex, item)
            mutable.mapIndexed { index, server -> server.copy(order = index) }
        }
    }

    private suspend fun updateList(transform: (List<DesktopServer>) -> List<DesktopServer>) {
        context.desktopDataStore.edit { prefs ->
            val raw = prefs[KEY_DESKTOPS] ?: "[]"
            val current = runCatching { json.decodeFromString<List<DesktopServer>>(raw) }.getOrDefault(emptyList())
            val updated = transform(current)
            prefs[KEY_DESKTOPS] = json.encodeToString(updated)
        }
    }
}
