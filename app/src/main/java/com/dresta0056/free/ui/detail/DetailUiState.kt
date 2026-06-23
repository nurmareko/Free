package com.dresta0056.free.ui.detail

import com.dresta0056.free.model.Item

data class DetailUiState(
    val item: Item? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val canDelete: Boolean = false,
    val showConfirm: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false
)
