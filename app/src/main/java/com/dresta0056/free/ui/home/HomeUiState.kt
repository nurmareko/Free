package com.dresta0056.free.ui.home

import com.dresta0056.free.model.Item

data class HomeUiState(
    val items: List<Item> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)
