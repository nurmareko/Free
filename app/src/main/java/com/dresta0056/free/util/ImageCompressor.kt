package com.dresta0056.free.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

suspend fun compressToImagePart(
    context: Context,
    uri: Uri,
    maxDim: Int = 1080,
    quality: Int = 80
): MultipartBody.Part = withContext(Dispatchers.IO) {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: throw IOException("Unable to open image")

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxDim = maxDim
        )
    }

    val decoded = resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOptions)
    } ?: throw IOException("Unable to decode image")

    val orientation = resolver.openInputStream(uri)?.use { stream ->
        ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val rotated = rotateForExif(decoded, orientation)
    if (rotated !== decoded) {
        decoded.recycle()
    }

    val scaled = scaleToMaxDim(rotated, maxDim)
    if (scaled !== rotated) {
        rotated.recycle()
    }

    val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { output ->
        if (!scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
            throw IOException("Unable to compress image")
        }
    }
    scaled.recycle()

    val body = file.asRequestBody("image/jpeg".toMediaType())
    MultipartBody.Part.createFormData("image", file.name, body)
}

private fun calculateInSampleSize(
    width: Int,
    height: Int,
    maxDim: Int
): Int {
    if (width <= 0 || height <= 0 || maxDim <= 0) return 1

    var inSampleSize = 1
    while (
        width / (inSampleSize * 2) >= maxDim &&
        height / (inSampleSize * 2) >= maxDim
    ) {
        inSampleSize *= 2
    }
    return inSampleSize
}

private fun rotateForExif(
    bitmap: Bitmap,
    orientation: Int
): Bitmap {
    val degrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (degrees == 0f) return bitmap

    val matrix = Matrix().apply {
        postRotate(degrees)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun scaleToMaxDim(
    bitmap: Bitmap,
    maxDim: Int
): Bitmap {
    val longestSide = maxOf(bitmap.width, bitmap.height)
    if (maxDim <= 0 || longestSide <= maxDim) return bitmap

    val scale = maxDim.toFloat() / longestSide.toFloat()
    val targetWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
    val targetHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
}
