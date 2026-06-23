package com.dresta0056.free.ui.preview

import com.dresta0056.free.model.Item

internal object PreviewData {
    val items = listOf(
        Item(
            id = "item-1",
            title = "Study Desk",
            description = "A sturdy desk with a wide writing surface and two side shelves.",
            location = "Library lobby",
            contactInfo = "avery@example.com",
            imageUrl = "",
            ownerId = "user-1",
            ownerName = "Avery",
            ownerEmail = "avery@example.com",
            createdAt = "2026-06-24T09:00:00Z"
        ),
        Item(
            id = "item-2",
            title = "Rice Cooker",
            description = "Works well and includes the original measuring cup.",
            location = "North dorm reception",
            contactInfo = "+1 555 010 4421",
            imageUrl = "",
            ownerId = "user-2",
            ownerName = "Mika",
            ownerEmail = "mika@example.com",
            createdAt = "2026-06-23T15:30:00Z"
        ),
        Item(
            id = "item-3",
            title = "Textbook Bundle",
            description = "Intro math and writing books from last semester.",
            location = "Student center",
            contactInfo = "sam@example.com",
            imageUrl = "",
            ownerId = "user-3",
            ownerName = "Sam",
            ownerEmail = "sam@example.com",
            createdAt = "2026-06-22T11:15:00Z"
        ),
        Item(
            id = "item-4",
            title = "Desk Lamp",
            description = "Adjustable LED lamp with three brightness settings.",
            location = "East hall",
            contactInfo = "+1 555 019 8842",
            imageUrl = "",
            ownerId = "user-4",
            ownerName = "Noor",
            ownerEmail = "noor@example.com",
            createdAt = "2026-06-21T18:45:00Z"
        )
    )

    val item = items.first()
}
