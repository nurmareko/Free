package com.dresta0056.free.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.network.toUserMessage
import com.dresta0056.free.network.SessionStore
import com.dresta0056.free.network.ApiService
import com.dresta0056.free.network.Network
import com.dresta0056.free.model.toDomain
import com.dresta0056.free.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val itemId: String,
    private val api: ApiService,
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
                        canDelete = it.item != null &&
                            profile != null &&
                            it.item.ownerId == profile.id
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
            try {
                api.deleteItem(itemId)
                _uiState.update { it.copy(deleted = true) }
            } catch (exception: Exception) {
                _uiState.update { it.copy(error = exception.toUserMessage()) }
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val item = api.getItem(itemId).toDomain()
                val profile = currentProfile
                _uiState.update {
                    it.copy(
                        item = item,
                        canDelete = profile != null && item.ownerId == profile.id,
                        isLoading = false
                    )
                }
            } catch (exception: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.toUserMessage()
                        )
                    }
            }
        }
    }

    class Factory(
        private val itemId: String,
        appContext: Context
    ) : ViewModelProvider.Factory {
        private val sessionStore = SessionStore(appContext.applicationContext)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(
                    itemId = itemId,
                    api = Network.api,
                    sessionStore = sessionStore
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
