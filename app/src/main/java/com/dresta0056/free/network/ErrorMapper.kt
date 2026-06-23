package com.dresta0056.free.network

import android.content.Context
import android.util.Log
import com.dresta0056.free.R
import java.io.IOException
import retrofit2.HttpException

fun Throwable.toUserMessage(context: Context): String = when (this) {
    is HttpException -> toHttpUserMessage(context)
    is IOException -> context.getString(R.string.error_no_internet)
    else -> context.getString(R.string.error_generic)
}

private fun HttpException.toHttpUserMessage(context: Context): String {
    val responseBody = response()?.errorBody()?.string()?.trim().orEmpty()
    if (responseBody.isNotBlank()) {
        Log.e("ApiError", "HTTP ${code()} response: $responseBody")
    }

    return when (code()) {
        401 -> context.getString(R.string.error_sign_in_again)
        403 -> context.getString(R.string.error_modify_own_items)
        404 -> context.getString(R.string.error_item_not_found)
        else -> responseBody
            .takeIf { it.isNotBlank() && it.length <= 160 }
            ?: context.getString(R.string.error_request_failed, code())
    }
}
