package com.dresta0056.free.data

import android.content.Context
import android.net.Uri
import com.dresta0056.free.model.Item
import com.dresta0056.free.model.toDomain
import com.dresta0056.free.network.ApiService
import com.dresta0056.free.network.Network
import com.dresta0056.free.network.SessionStore
import com.dresta0056.free.network.toTextPart
import com.dresta0056.free.sync.PendingItemSyncWorker
import com.dresta0056.free.util.compressToImagePart
import com.dresta0056.free.util.copyImageToPrivateFile
import com.dresta0056.free.util.deletePrivateImage
import java.io.IOException
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ImageProcessingException(cause: Throwable) : Exception(cause)

sealed interface CreateItemResult {
    data class Uploaded(val item: Item) : CreateItemResult
    data class Queued(val item: Item) : CreateItemResult
    data class Error(val throwable: Throwable) : CreateItemResult
}

class ItemRepository(
    private val appContext: Context,
    private val api: ApiService,
    private val sessionStore: SessionStore,
    private val localStore: LocalItemStore
) {
    fun observeItems(): Flow<List<Item>> = localStore.observeItems()

    fun observeMyItems(): Flow<List<Item>> = combine(
        localStore.observeItems(),
        sessionStore.profile
    ) { items, profile ->
        val userId = profile?.id
        items.filter { item -> userId != null && item.ownerId == userId }
    }

    fun observeItem(id: String): Flow<Item?> = localStore.observeItem(id)

    suspend fun refreshItems(): Throwable? {
        PendingItemSyncWorker.enqueue(appContext)
        return try {
            val items = api.getItems().map { it.toDomain() }
            localStore.replaceRemoteItems(items)
            null
        } catch (throwable: Throwable) {
            throwable
        }
    }

    suspend fun refreshMyItems(): Throwable? {
        PendingItemSyncWorker.enqueue(appContext)
        return try {
            val items = api.getItems(mine = true).map { it.toDomain() }
            localStore.upsertRemoteItems(items)
            null
        } catch (throwable: Throwable) {
            throwable
        }
    }

    suspend fun refreshItem(id: String): Throwable? {
        if (id.startsWith(PENDING_ID_PREFIX)) return null

        return try {
            localStore.upsertRemoteItems(listOf(api.getItem(id).toDomain()))
            null
        } catch (throwable: Throwable) {
            throwable
        }
    }

    suspend fun deleteItem(id: String): Throwable? {
        return try {
            api.deleteItem(id)
            localStore.removeItem(id)
            null
        } catch (throwable: Throwable) {
            throwable
        }
    }

    suspend fun createItem(
        title: String,
        description: String,
        location: String,
        contactInfo: String,
        imageUri: Uri
    ): CreateItemResult {
        val privateImageUri = try {
            copyImageToPrivateFile(appContext, imageUri)
        } catch (throwable: Throwable) {
            return CreateItemResult.Error(ImageProcessingException(throwable))
        }

        val uploaded = uploadCreate(
            title = title,
            description = description,
            location = location,
            contactInfo = contactInfo,
            imageUri = privateImageUri
        )

        return when (uploaded) {
            is CreateItemResult.Uploaded -> {
                deletePrivateImage(privateImageUri.toString())
                localStore.upsertRemoteItems(listOf(uploaded.item))
                uploaded
            }

            is CreateItemResult.Error -> {
                if (uploaded.throwable is IOException) {
                    val queued = queueCreate(
                        title = title,
                        description = description,
                        location = location,
                        contactInfo = contactInfo,
                        imageUri = privateImageUri.toString()
                    )
                    PendingItemSyncWorker.enqueue(appContext)
                    queued
                } else {
                    deletePrivateImage(privateImageUri.toString())
                    uploaded
                }
            }

            is CreateItemResult.Queued -> uploaded
        }
    }

    private suspend fun uploadCreate(
        title: String,
        description: String,
        location: String,
        contactInfo: String,
        imageUri: Uri
    ): CreateItemResult {
        val image = try {
            compressToImagePart(appContext, imageUri)
        } catch (throwable: Throwable) {
            return CreateItemResult.Error(ImageProcessingException(throwable))
        }

        return try {
            val item = api.createItem(
                title = title.toTextPart(),
                description = description.toTextPart(),
                location = location.toTextPart(),
                contactInfo = contactInfo.toTextPart(),
                image = image
            ).toDomain()
            CreateItemResult.Uploaded(item)
        } catch (throwable: Throwable) {
            CreateItemResult.Error(throwable)
        }
    }

    private suspend fun queueCreate(
        title: String,
        description: String,
        location: String,
        contactInfo: String,
        imageUri: String
    ): CreateItemResult.Queued {
        val profile = sessionStore.profile.first()
        val localId = "$PENDING_ID_PREFIX${UUID.randomUUID()}"
        val pending = PendingCreateItem(
            localId = localId,
            title = title,
            description = description,
            location = location,
            contactInfo = contactInfo,
            imageUri = imageUri,
            ownerId = profile?.id.orEmpty(),
            ownerName = profile?.name?.takeIf { it.isNotBlank() } ?: profile?.email.orEmpty(),
            ownerEmail = profile?.email.orEmpty(),
            createdAt = Instant.now().toString()
        )
        localStore.addPendingCreate(pending)
        return CreateItemResult.Queued(
            Item(
                id = pending.localId,
                title = pending.title,
                description = pending.description,
                location = pending.location,
                contactInfo = pending.contactInfo,
                imageUrl = pending.imageUri,
                ownerId = pending.ownerId,
                ownerName = pending.ownerName,
                ownerEmail = pending.ownerEmail,
                createdAt = pending.createdAt,
                isPending = true
            )
        )
    }

    companion object {
        const val PENDING_ID_PREFIX = "pending-"
    }
}

object ItemRepositoryProvider {
    @Volatile
    private var instance: ItemRepository? = null

    fun get(context: Context): ItemRepository {
        val appContext = context.applicationContext
        return instance ?: synchronized(this) {
            instance ?: ItemRepository(
                appContext = appContext,
                api = Network.api,
                sessionStore = SessionStore(appContext),
                localStore = LocalItemStore(appContext)
            ).also {
                instance = it
                PendingItemSyncWorker.enqueue(appContext)
            }
        }
    }
}
