package com.dresta0056.free.ui.edit

import android.net.Uri

data class EditItemUiState(
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val contactInfo: String = "",
    val existingImageUrl: String? = null,
    val newImageUri: Uri? = null,
    val loaded: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
    val locationError: Boolean = false,
    val contactError: Boolean = false
)
