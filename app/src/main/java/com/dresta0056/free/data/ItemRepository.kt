package com.dresta0056.free.data

import com.dresta0056.free.core.AppResult
import com.dresta0056.free.core.runCatchingResult
import com.dresta0056.free.data.local.ItemDao
import com.dresta0056.free.data.remote.ApiService
import com.dresta0056.free.data.remote.toTextPart
import com.dresta0056.free.domain.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody

class ItemRepository(
    private val api: ApiService,
    private val dao: ItemDao
) {
    fun observeItems(): Flow<List<Item>> =
        dao.observeAll().map { it.toDomains() }

    fun observeMyItems(ownerId: String): Flow<List<Item>> =
        dao.observeByOwner(ownerId).map { it.toDomains() }

    fun observeItem(id: String): Flow<Item?> =
        dao.observeById(id).map { it?.toDomain() }

    suspend fun refreshItems(): AppResult<Unit> = runCatchingResult {
        val items = api.getItems()
        dao.upsert(items.toEntities())
    }

    suspend fun refreshMyItems(): AppResult<Unit> = runCatchingResult {
        val items = api.getItems(mine = true)
        dao.upsert(items.toEntities())
    }

    suspend fun refreshItem(id: String): AppResult<Unit> = runCatchingResult {
        val item = api.getItem(id)
        dao.upsert(item.toEntity())
    }

    suspend fun addItem(
        title: String,
        description: String,
        location: String,
        contactInfo: String,
        image: MultipartBody.Part
    ): AppResult<Item> = runCatchingResult {
        val item = api.createItem(
            title = title.toTextPart(),
            description = description.toTextPart(),
            location = location.toTextPart(),
            contactInfo = contactInfo.toTextPart(),
            image = image
        )
        dao.upsert(item.toEntity())
        item.toDomain()
    }

    suspend fun updateItem(
        id: String,
        title: String,
        description: String,
        location: String,
        contactInfo: String,
        image: MultipartBody.Part?
    ): AppResult<Item> = runCatchingResult {
        val item = api.updateItem(
            id = id,
            title = title.toTextPart(),
            description = description.toTextPart(),
            location = location.toTextPart(),
            contactInfo = contactInfo.toTextPart(),
            image = image
        )
        dao.upsert(item.toEntity())
        item.toDomain()
    }

    suspend fun deleteItem(id: String): AppResult<Unit> = runCatchingResult {
        api.deleteItem(id)
        dao.deleteById(id)
    }
}
