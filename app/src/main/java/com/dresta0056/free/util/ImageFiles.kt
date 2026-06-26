package com.dresta0056.free.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun copyImageToPrivateFile(context: Context, source: Uri): Uri = withContext(Dispatchers.IO) {
    val dir = File(context.filesDir, "pending-images").also { it.mkdirs() }
    val target = File(dir, "${UUID.randomUUID()}.jpg")
    context.contentResolver.openInputStream(source)?.use { input ->
        target.outputStream().use { output ->
            input.copyTo(output)
        }
    } ?: throw IOException("Unable to open selected image")
    Uri.fromFile(target)
}

fun deletePrivateImage(uri: String) {
    val parsed = Uri.parse(uri)
    if (parsed.scheme == "file") {
        runCatching { File(parsed.path.orEmpty()).delete() }
    }
}
