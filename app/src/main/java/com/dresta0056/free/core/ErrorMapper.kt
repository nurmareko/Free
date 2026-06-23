package com.dresta0056.free.core

import java.io.IOException
import retrofit2.HttpException

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> when (code()) {
        401 -> "Please sign in again"
        403 -> "You can only modify your own items"
        404 -> "Item not found"
        else -> "Request failed (${code()})"
    }
    is IOException -> "No internet connection"
    else -> "Something went wrong"
}
