package com.dresta0056.free.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
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
    val decodedImage = decodeImage(context, uri, maxDim)
    val decoded = decodedImage.bitmap

    val orientation = if (decodedImage.needsExifRotation) {
        try {
            resolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    } else {
        ExifInterface.ORIENTATION_NORMAL
    }

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

private data class DecodedImage(
    val bitmap: Bitmap,
    val needsExifRotation: Boolean
)

private fun decodeImage(
    context: Context,
    uri: Uri,
    maxDim: Int
): DecodedImage {
    decodeWithBitmapFactory(context, uri, maxDim)?.let { bitmap ->
        return DecodedImage(bitmap = bitmap, needsExifRotation = true)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return DecodedImage(
            bitmap = decodeWithImageDecoder(context, uri, maxDim),
            needsExifRotation = false
        )
    }

    throw IOException("Unable to decode image")
}

private fun decodeWithBitmapFactory(
    context: Context,
    uri: Uri,
    maxDim: Int
): Bitmap? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, bounds)
    } ?: throw IOException("Unable to open image")

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
        return null
    }

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxDim = maxDim
        )
    }

    return resolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOptions)
    } ?: throw IOException("Unable to open image")
}

private fun decodeWithImageDecoder(
    context: Context,
    uri: Uri,
    maxDim: Int
): Bitmap {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

        val width = info.size.width
        val height = info.size.height
        val longestSide = maxOf(width, height)
        if (maxDim > 0 && longestSide > maxDim) {
            val scale = maxDim.toFloat() / longestSide.toFloat()
            decoder.setTargetSize(
                (width * scale).toInt().coerceAtLeast(1),
                (height * scale).toInt().coerceAtLeast(1)
            )
        }
    }
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
