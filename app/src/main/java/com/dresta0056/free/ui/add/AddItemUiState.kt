package com.dresta0056.free.ui.add

import android.net.Uri

data class AddItemUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    val imageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val locationError: Boolean = false,
    val contactError: Boolean = false,
    val imageError: Boolean = false
)
