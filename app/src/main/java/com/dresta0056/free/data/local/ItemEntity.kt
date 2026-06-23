package com.dresta0056.free.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "items", indices = [Index("ownerId"), Index("createdAt")])
data class ItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val location: String,
    val contactInfo: String,
    val imageUrl: String,
    val ownerId: String,
    val ownerName: String,
    val ownerEmail: String,
    val createdAt: String
)
