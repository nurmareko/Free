package com.dresta0056.free.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.core.AppResult
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.auth.SessionStore
import com.dresta0056.free.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val itemId: String,
    private val repo: ItemRepository,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repo.observeItem(itemId),
                sessionStore.profile
            ) { item, profile ->
                item to (item != null && profile != null && item.ownerId == profile.id)
            }.collect { (item, canDelete) ->
                _uiState.update {
                    it.copy(
                        item = item,
                        canDelete = canDelete
                    )
                }
            }
        }

        refresh()
    }

    fun askDelete() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(showConfirm = true, error = null) }
    }

    fun dismissDelete() {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(showConfirm = false) }
    }

    fun confirmDelete() {
        if (_uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            when (val result = repo.deleteItem(itemId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(deleted = true) }
                }

                is AppResult.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
            }
            _uiState.update {
                it.copy(
                    isDeleting = false,
                    showConfirm = false
                )
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.refreshItem(itemId)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    class Factory(
        private val itemId: String,
        appContext: Context
    ) : ViewModelProvider.Factory {
        private val repo = ServiceLocator.provideRepository(appContext.applicationContext)
        private val sessionStore = ServiceLocator.provideSessionStore(appContext.applicationContext)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(
                    itemId = itemId,
                    repo = repo,
                    sessionStore = sessionStore
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
