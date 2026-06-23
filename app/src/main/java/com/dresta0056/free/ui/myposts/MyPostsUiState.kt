package com.dresta0056.free.ui.myposts

import com.dresta0056.free.domain.Item

data class MyPostsUiState(
    val items: List<Item> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null
)
