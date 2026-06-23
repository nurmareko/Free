package com.dresta0056.free.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dresta0056.free.ui.auth.AuthUiState
import com.dresta0056.free.ui.auth.AuthViewModel
import com.dresta0056.free.ui.login.LoginScreen
import com.dresta0056.free.ui.nav.HomeNavHost

@Composable
fun App() {
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(ctx.applicationContext)
    )
    val state by vm.state.collectAsState()
    val signingIn by vm.signingIn.collectAsState()
    val error by vm.errorMessage.collectAsState()

    val signedInState = state as? AuthUiState.SignedIn
    if (signedInState != null) {
        LaunchedEffect(signedInState.profile.id) {
            vm.trySilentRefresh(ctx)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val authState = state) {
            AuthUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            AuthUiState.SignedOut -> {
                LoginScreen(
                    signingIn = signingIn,
                    error = error,
                    onSignIn = { vm.signIn(ctx) }
                )
            }

            is AuthUiState.SignedIn -> {
                HomeNavHost(
                    rootProfile = authState.profile,
                    onLogout = { vm.signOut(ctx) }
                )
            }
        }
    }
}
