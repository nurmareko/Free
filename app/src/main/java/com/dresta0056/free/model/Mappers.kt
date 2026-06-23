package com.dresta0056.free.model

import com.dresta0056.free.network.dto.ItemDto

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
