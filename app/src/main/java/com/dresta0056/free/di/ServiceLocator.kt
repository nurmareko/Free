package com.dresta0056.free.di

import android.content.Context
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.local.AppDatabase
import com.dresta0056.free.data.remote.Network

object ServiceLocator {
    @Volatile
    private var repository: ItemRepository? = null

    fun provideRepository(context: Context): ItemRepository {
        return repository ?: synchronized(this) {
            repository ?: ItemRepository(
                api = Network.api,
                dao = AppDatabase.getInstance(context).itemDao()
            ).also { repository = it }
        }
    }
}
