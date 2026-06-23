package com.dresta0056.free.network

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun String.toTextPart(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())
