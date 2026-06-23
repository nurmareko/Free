package com.dresta0056.free.ui.auth

import com.dresta0056.free.model.UserProfile

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object SignedOut : AuthUiState
    data class SignedIn(val profile: UserProfile) : AuthUiState
}
