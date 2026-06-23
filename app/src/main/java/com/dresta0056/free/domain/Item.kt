package com.dresta0056.free.domain

data class Item(
    val id: String,
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
