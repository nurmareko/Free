package com.dresta0056.free.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.ItemRepositoryProvider
import com.dresta0056.free.network.toUserMessage
import com.dresta0056.free.network.SessionStore
import com.dresta0056.free.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val itemId: String,
    private val appContext: Context,
    private val repository: ItemRepository,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    private var currentProfile: UserProfile? = null

    init {
        viewModelScope.launch {
            sessionStore.profile.collect { profile ->
                currentProfile = profile
                _uiState.update {
                    it.copy(
                        canDelete = canModify(it.item, profile)
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observeItem(itemId).collect { item ->
                val profile = currentProfile
                _uiState.update {
                    it.copy(
                        item = item,
                        canDelete = canModify(item, profile),
                        isLoading = false
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

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            val error = repository.deleteItem(itemId)
            if (error == null) {
                _uiState.update { it.copy(deleted = true) }
            } else {
                _uiState.update { it.copy(error = error.toUserMessage(appContext)) }
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
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = it.item == null, error = null) }
            val error = repository.refreshItem(itemId)
            if (error != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.toUserMessage(appContext)
                    )
                }
            }
        }
    }

    private fun canModify(item: com.dresta0056.free.model.Item?, profile: UserProfile?): Boolean =
        item != null && !item.isPending && profile != null && item.ownerId == profile.id

    class Factory(
        private val itemId: String,
        appContext: Context
    ) : ViewModelProvider.Factory {
        private val applicationContext = appContext.applicationContext
        private val sessionStore = SessionStore(applicationContext)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(
                    itemId = itemId,
                    appContext = applicationContext,
                    repository = ItemRepositoryProvider.get(applicationContext),
                    sessionStore = sessionStore
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
