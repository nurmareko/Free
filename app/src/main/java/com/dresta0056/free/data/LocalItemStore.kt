package com.dresta0056.free.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dresta0056.free.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class PendingCreateItem(
    val localId: String,
    val title: String,
    val description: String,
    val location: String,
    val contactInfo: String,
    val imageUri: String,
    val ownerId: String,
    val ownerName: String,
    val ownerEmail: String,
    val createdAt: String
)

class LocalItemStore(context: Context) {
    private val dbHelper = ItemDbHelper(context.applicationContext)

    init {
        changes.tryEmit(Unit)
    }

    fun observeItems(): Flow<List<Item>> = changes.map {
        withContext(Dispatchers.IO) { getItemsNow() }
    }

    fun observeItem(id: String): Flow<Item?> = changes.map {
        withContext(Dispatchers.IO) { getItemNow(id) }
    }

    suspend fun replaceRemoteItems(items: List<Item>) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.transaction {
            delete(ItemDbHelper.ITEMS, "is_pending = 0", null)
            items.forEach { insertOrReplaceItem(it.copy(isPending = false)) }
        }
        notifyChanged()
    }

    suspend fun upsertRemoteItems(items: List<Item>) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.transaction {
            items.forEach { insertOrReplaceItem(it.copy(isPending = false)) }
        }
        notifyChanged()
    }

    suspend fun addPendingCreate(pending: PendingCreateItem) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.transaction {
            insertOrReplaceItem(pending.toLocalItem())
            replace(ItemDbHelper.PENDING_CREATES, null, pending.toPendingValues())
        }
        notifyChanged()
    }

    suspend fun pendingCreates(): List<PendingCreateItem> = withContext(Dispatchers.IO) {
        dbHelper.readableDatabase.query(
            ItemDbHelper.PENDING_CREATES,
            null,
            null,
            null,
            null,
            null,
            "created_at ASC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toPendingCreate())
                }
            }
        }
    }

    suspend fun replacePendingWithRemote(localId: String, remote: Item) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.transaction {
            delete(ItemDbHelper.PENDING_CREATES, "local_id = ?", arrayOf(localId))
            delete(ItemDbHelper.ITEMS, "id = ?", arrayOf(localId))
            insertOrReplaceItem(remote.copy(isPending = false))
        }
        notifyChanged()
    }

    suspend fun removeItem(id: String) = withContext(Dispatchers.IO) {
        dbHelper.writableDatabase.delete(ItemDbHelper.ITEMS, "id = ?", arrayOf(id))
        notifyChanged()
    }

    private fun getItemsNow(): List<Item> {
        return dbHelper.readableDatabase.query(
            ItemDbHelper.ITEMS,
            null,
            null,
            null,
            null,
            null,
            "is_pending DESC, created_at DESC"
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toItem())
                }
            }
        }
    }

    private fun getItemNow(id: String): Item? {
        return dbHelper.readableDatabase.query(
            ItemDbHelper.ITEMS,
            null,
            "id = ?",
            arrayOf(id),
            null,
            null,
            null,
            "1"
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toItem() else null
        }
    }

    private fun SQLiteDatabase.insertOrReplaceItem(item: Item) {
        replace(ItemDbHelper.ITEMS, null, item.toValues())
    }

    private fun notifyChanged() {
        changes.tryEmit(Unit)
    }

    private companion object {
        val changes = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)
    }
}

private class ItemDbHelper(context: Context) : SQLiteOpenHelper(context, "free_items.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $ITEMS (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                location TEXT NOT NULL,
                contact_info TEXT NOT NULL,
                image_url TEXT NOT NULL,
                owner_id TEXT NOT NULL,
                owner_name TEXT NOT NULL,
                owner_email TEXT NOT NULL,
                created_at TEXT NOT NULL,
                is_pending INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE $PENDING_CREATES (
                local_id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                location TEXT NOT NULL,
                contact_info TEXT NOT NULL,
                image_uri TEXT NOT NULL,
                owner_id TEXT NOT NULL,
                owner_name TEXT NOT NULL,
                owner_email TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $PENDING_CREATES")
        db.execSQL("DROP TABLE IF EXISTS $ITEMS")
        onCreate(db)
    }

    companion object {
        const val ITEMS = "items"
        const val PENDING_CREATES = "pending_creates"
    }
}

private inline fun SQLiteDatabase.transaction(block: SQLiteDatabase.() -> Unit) {
    beginTransaction()
    try {
        block()
        setTransactionSuccessful()
    } finally {
        endTransaction()
    }
}

private fun Item.toValues(): ContentValues = ContentValues().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("location", location)
    put("contact_info", contactInfo)
    put("image_url", imageUrl)
    put("owner_id", ownerId)
    put("owner_name", ownerName)
    put("owner_email", ownerEmail)
    put("created_at", createdAt)
    put("is_pending", if (isPending) 1 else 0)
}

private fun PendingCreateItem.toPendingValues(): ContentValues = ContentValues().apply {
    put("local_id", localId)
    put("title", title)
    put("description", description)
    put("location", location)
    put("contact_info", contactInfo)
    put("image_uri", imageUri)
    put("owner_id", ownerId)
    put("owner_name", ownerName)
    put("owner_email", ownerEmail)
    put("created_at", createdAt)
}

private fun PendingCreateItem.toLocalItem(): Item = Item(
    id = localId,
    title = title,
    description = description,
    location = location,
    contactInfo = contactInfo,
    imageUrl = imageUri,
    ownerId = ownerId,
    ownerName = ownerName,
    ownerEmail = ownerEmail,
    createdAt = createdAt,
    isPending = true
)

private fun Cursor.toItem(): Item = Item(
    id = getString(column("id")),
    title = getString(column("title")),
    description = getString(column("description")),
    location = getString(column("location")),
    contactInfo = getString(column("contact_info")),
    imageUrl = getString(column("image_url")),
    ownerId = getString(column("owner_id")),
    ownerName = getString(column("owner_name")),
    ownerEmail = getString(column("owner_email")),
    createdAt = getString(column("created_at")),
    isPending = getInt(column("is_pending")) == 1
)

private fun Cursor.toPendingCreate(): PendingCreateItem = PendingCreateItem(
    localId = getString(column("local_id")),
    title = getString(column("title")),
    description = getString(column("description")),
    location = getString(column("location")),
    contactInfo = getString(column("contact_info")),
    imageUri = getString(column("image_uri")),
    ownerId = getString(column("owner_id")),
    ownerName = getString(column("owner_name")),
    ownerEmail = getString(column("owner_email")),
    createdAt = getString(column("created_at"))
)

private fun Cursor.column(name: String): Int = getColumnIndexOrThrow(name)
