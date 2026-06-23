package com.dresta0056.free.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun observeByOwner(ownerId: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :id")
    fun observeById(id: String): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: String): ItemEntity?

    @Upsert
    suspend fun upsert(items: List<ItemEntity>)

    @Upsert
    suspend fun upsert(item: ItemEntity)

    @Query("DELETE FROM items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM items")
    suspend fun clear()
}
