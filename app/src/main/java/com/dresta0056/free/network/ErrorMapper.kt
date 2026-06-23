package com.dresta0056.free.network

import android.util.Log
import java.io.IOException
import retrofit2.HttpException

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> toHttpUserMessage()
    is IOException -> "No internet connection"
    else -> "Something went wrong"
}

private fun HttpException.toHttpUserMessage(): String {
    val responseBody = response()?.errorBody()?.string()?.trim().orEmpty()
    if (responseBody.isNotBlank()) {
        Log.e("ApiError", "HTTP ${code()} response: $responseBody")
    }

    return when (code()) {
        401 -> "Please sign in again"
        403 -> "You can only modify your own items"
        404 -> "Item not found"
        else -> responseBody
            .takeIf { it.isNotBlank() && it.length <= 160 }
            ?: "Request failed (${code()})"
    }
}
