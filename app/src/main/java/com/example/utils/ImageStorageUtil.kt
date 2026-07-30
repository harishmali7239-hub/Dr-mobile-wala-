package com.example.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageStorageUtil {

    fun getPhotoFileUriForCamera(context: Context): Pair<Uri, File> {
        val photosDir = File(context.filesDir, "repair_photos")
        if (!photosDir.exists()) photosDir.mkdirs()
        val file = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(uri, file)
    }

    fun saveImageFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "repair_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val destFile = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            val outputStream = FileOutputStream(destFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val photosDir = File(context.filesDir, "repair_photos")
            if (!photosDir.exists()) photosDir.mkdirs()

            val destFile = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
            FileOutputStream(destFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
