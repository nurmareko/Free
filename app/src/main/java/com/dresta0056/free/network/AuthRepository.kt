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
import com.dresta0056.free.R
import com.dresta0056.free.network.AppResult
import com.dresta0056.free.network.toUserMessage
import com.dresta0056.free.network.ApiService
import com.dresta0056.free.network.AuthSession
import com.dresta0056.free.model.UserProfile
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class AuthRepository(
    private val appContext: Context,
    private val api: ApiService,
    private val store: SessionStore
) {
    private val cm = CredentialManager.create(appContext.applicationContext)

    val sessionProfile: Flow<UserProfile?> = store.profile

    suspend fun restoreSession(): AppResult<UserProfile?> {
        store.profile.first() ?: return AppResult.Success(null)
        val cachedSession = store.session.first()
        if (cachedSession != null) {
            AuthSession.idToken = cachedSession.idToken
            return AppResult.Success(cachedSession.profile)
        }

        AuthSession.idToken = null
        store.clear()
        return AppResult.Error(appContext.getString(R.string.error_sign_in_again))
    }

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
            store.save(profile, token)
            AppResult.Success(profile)
        } catch (throwable: Throwable) {
            AuthSession.idToken = null
            when (throwable) {
                is GetCredentialCancellationException -> AppResult.Error(
                    appContext.getString(R.string.error_sign_in_cancelled),
                    throwable
                )
                is NoCredentialException -> AppResult.Error(
                    appContext.getString(R.string.error_no_google_account),
                    throwable
                )
                is UnexpectedCredentialException -> AppResult.Error(
                    appContext.getString(R.string.error_unexpected_credential),
                    throwable
                )
                is HttpException, is IOException -> AppResult.Error(
                    throwable.toUserMessage(appContext),
                    throwable
                )
                else -> AppResult.Error(throwable.toUserMessage(appContext), throwable)
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
