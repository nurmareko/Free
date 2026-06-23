package com.dresta0056.free.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dresta0056.free.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("session")

class SessionStore(appContext: Context) {
    private val dataStore = appContext.applicationContext.dataStore

    val profile: Flow<UserProfile?> = dataStore.data.map { preferences ->
        val id = preferences[USER_ID] ?: return@map null
        UserProfile(
            id = id,
            email = preferences[EMAIL].orEmpty(),
            name = preferences[NAME].orEmpty(),
            pictureUrl = preferences[PICTURE].orEmpty()
        )
    }

    suspend fun save(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = profile.id
            preferences[EMAIL] = profile.email
            preferences[NAME] = profile.name
            preferences[PICTURE] = profile.pictureUrl
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val NAME = stringPreferencesKey("name")
        val PICTURE = stringPreferencesKey("picture")
    }
}
