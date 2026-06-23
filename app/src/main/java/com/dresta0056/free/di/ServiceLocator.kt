package com.dresta0056.free.di

import android.content.Context
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.auth.AuthRepository
import com.dresta0056.free.data.auth.SessionStore
import com.dresta0056.free.data.local.AppDatabase
import com.dresta0056.free.data.remote.Network

object ServiceLocator {
    @Volatile
    private var repository: ItemRepository? = null
    @Volatile
    private var sessionStore: SessionStore? = null
    @Volatile
    private var authRepository: AuthRepository? = null

    fun provideRepository(context: Context): ItemRepository {
        return repository ?: synchronized(this) {
            repository ?: ItemRepository(
                api = Network.api,
                dao = AppDatabase.getInstance(context).itemDao()
            ).also { repository = it }
        }
    }

    fun provideSessionStore(context: Context): SessionStore {
        return sessionStore ?: synchronized(this) {
            sessionStore ?: SessionStore(context.applicationContext).also { sessionStore = it }
        }
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: AuthRepository(
                appContext = context.applicationContext,
                api = Network.api,
                store = provideSessionStore(context)
            ).also { authRepository = it }
        }
    }
}
