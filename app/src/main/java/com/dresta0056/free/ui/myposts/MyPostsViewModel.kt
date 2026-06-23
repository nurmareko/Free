package com.dresta0056.free.ui.myposts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.core.AppResult
import com.dresta0056.free.data.ItemRepository
import com.dresta0056.free.data.auth.SessionStore
import com.dresta0056.free.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MyPostsViewModel(
    private val repo: ItemRepository,
    private val sessionStore: SessionStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionStore.profile
                .flatMapLatest { profile ->
                    if (profile == null) {
                        flowOf(emptyList())
                    } else {
                        repo.observeMyItems(profile.id)
                    }
                }
                .collect { items ->
                    _uiState.update { it.copy(items = items) }
                }
        }

        viewModelScope.launch {
            sessionStore.profile
                .filterNotNull()
                .take(1)
                .collect {
                    refresh()
                }
        }
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            when (val result = repo.refreshMyItems()) {
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isRefreshing = false, error = result.message)
                    }
                }

                is AppResult.Success -> {
                    _uiState.update { it.copy(isRefreshing = false) }
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
        private val repo = ServiceLocator.provideRepository(appContext.applicationContext)
        private val sessionStore = ServiceLocator.provideSessionStore(appContext.applicationContext)

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MyPostsViewModel::class.java)) {
                return MyPostsViewModel(repo, sessionStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
