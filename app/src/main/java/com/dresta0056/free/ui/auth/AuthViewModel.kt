package com.dresta0056.free.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dresta0056.free.network.AppResult
import com.dresta0056.free.network.AuthRepository
import com.dresta0056.free.network.Network
import com.dresta0056.free.network.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _signingIn = MutableStateFlow(false)
    val signingIn: StateFlow<Boolean> = _signingIn.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.sessionProfile.collect { profile ->
                _state.value = if (profile != null) {
                    AuthUiState.SignedIn(profile)
                } else {
                    AuthUiState.SignedOut
                }
            }
        }
    }

    fun trySilentRefresh(ctx: Context) {
        viewModelScope.launch {
            repo.trySilentSignIn(ctx)
        }
    }

    fun signIn(ctx: Context) {
        viewModelScope.launch {
            _signingIn.value = true
            _errorMessage.value = null
            when (val result = repo.signIn(ctx)) {
                is AppResult.Success -> _state.value = AuthUiState.SignedIn(result.data)
                is AppResult.Error -> _errorMessage.value = result.message
            }
            _signingIn.value = false
        }
    }

    fun signOut(ctx: Context) {
        viewModelScope.launch {
            repo.signOut(ctx)
        }
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    class Factory(private val appContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                val sessionStore = SessionStore(appContext.applicationContext)
                val authRepository = AuthRepository(
                    appContext = appContext.applicationContext,
                    api = Network.api,
                    store = sessionStore
                )
                return AuthViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
