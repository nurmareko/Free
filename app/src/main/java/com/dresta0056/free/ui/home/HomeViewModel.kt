package com.dresta0056.free.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.ItemRepositoryProvider
import com.dresta0056.free.network.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(
    private val appContext: Context,
    private val repository: ItemRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeItems().collect { items ->
                _uiState.update { it.copy(items = items) }
            }
        }
        refresh()
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            val error = repository.refreshItems()
            _uiState.update {
                if (error == null) {
                    it.copy(isRefreshing = false)
                } else {
                    it.copy(
                        isRefreshing = false,
                        error = error.toUserMessage(appContext)
                    )
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        appContext: Context
    ) : ViewModelProvider.Factory {
        private val applicationContext = appContext.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(
                    applicationContext,
                    ItemRepositoryProvider.get(applicationContext)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
