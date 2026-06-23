package com.dresta0056.free.network

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.dresta0056.free.BuildConfig
import com.dresta0056.free.network.AppResult
import com.dresta0056.free.network.toUserMessage
import com.dresta0056.free.network.ApiService
import com.dresta0056.free.network.AuthSession
import com.dresta0056.free.model.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class AuthRepository(
    appContext: Context,
    private val api: ApiService,
    private val store: SessionStore
) {
    private val cm = CredentialManager.create(appContext.applicationContext)

    val sessionProfile: Flow<UserProfile?> = store.profile

    suspend fun signIn(activityContext: Context): AppResult<UserProfile> {
        return try {
            val option = GetSignInWithGoogleOption.Builder(BuildConfig.WEB_CLIENT_ID).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
            val response = cm.getCredential(activityContext, request)
            val token = response.idToken()

            AuthSession.idToken = token
            val me = api.me()
            val profile = UserProfile(
                id = me.id,
                email = me.email,
                name = me.name,
                pictureUrl = me.picture
            )
            store.save(profile)
            AppResult.Success(profile)
        } catch (throwable: Throwable) {
            AuthSession.idToken = null
            when (throwable) {
                is GetCredentialCancellationException -> AppResult.Error("Sign-in cancelled", throwable)
                is NoCredentialException -> AppResult.Error("No Google account available", throwable)
                is UnexpectedCredentialException -> AppResult.Error("Unexpected credential type", throwable)
                is HttpException, is IOException -> AppResult.Error(throwable.toUserMessage(), throwable)
                else -> AppResult.Error(throwable.toUserMessage(), throwable)
            }
        }
    }

    suspend fun trySilentSignIn(activityContext: Context): AppResult<Unit> {
        return try {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
            val response = cm.getCredential(activityContext, request)

            AuthSession.idToken = response.idToken()
            AppResult.Success(Unit)
        } catch (throwable: Throwable) {
            when (throwable) {
                is NoCredentialException -> AppResult.Error("No Google account available", throwable)
                is UnexpectedCredentialException -> AppResult.Error("Unexpected credential type", throwable)
                else -> AppResult.Error(throwable.toUserMessage(), throwable)
            }
        }
    }

    suspend fun signOut(activityContext: Context) {
        AuthSession.idToken = null
        store.clear()
        runCatching { cm.clearCredentialState(ClearCredentialStateRequest()) }
    }

    private fun GetCredentialResponse.idToken(): String {
        val cred = credential
        if (
            cred is CustomCredential &&
            cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(cred.data).idToken
        }
        throw UnexpectedCredentialException()
    }

    private class UnexpectedCredentialException : Exception()
}
