package com.dresta0056.free.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val idToken = AuthSession.idToken
        val request = if (idToken != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $idToken")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
