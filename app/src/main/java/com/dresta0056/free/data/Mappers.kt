package com.dresta0056.free.data

import com.dresta0056.free.data.local.ItemEntity
import com.dresta0056.free.data.remote.dto.ItemDto
import com.dresta0056.free.domain.Item

fun ItemDto.toEntity(): ItemEntity = ItemEntity(
    id = id,
    title = title,
    description = description,
    location = location,
    contactInfo = contactInfo,
    imageUrl = imageUrl,
    ownerId = ownerId,
    ownerName = ownerName,
    ownerEmail = ownerEmail,
    createdAt = createdAt
)

fun ItemEntity.toDomain(): Item = Item(
    id = id,
    title = title,
    description = description,
    location = location,
    contactInfo = contactInfo,
    imageUrl = imageUrl,
    ownerId = ownerId,
    ownerName = ownerName,
    ownerEmail = ownerEmail,
    createdAt = createdAt
)

fun ItemDto.toDomain(): Item = Item(
    id = id,
    title = title,
    description = description,
    location = location,
    contactInfo = contactInfo,
    imageUrl = imageUrl,
    ownerId = ownerId,
    ownerName = ownerName,
    ownerEmail = ownerEmail,
    createdAt = createdAt
)

fun List<ItemDto>.toEntities(): List<ItemEntity> = map { it.toEntity() }

fun List<ItemEntity>.toDomains(): List<Item> = map { it.toDomain() }
