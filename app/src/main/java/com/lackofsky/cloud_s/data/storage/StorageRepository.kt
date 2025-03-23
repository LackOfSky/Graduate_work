package com.lackofsky.cloud_s.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

class StorageRepository {
    fun saveFileFromUri(context: Context, uri: Uri, fileName: String,folder: UserInfoStorageFolder): Uri? {
        val directory = File(context.filesDir,  folder.folderName).apply { mkdirs() }
        try {
            // Создаём файл в приватной директории приложения
            val mimeType: String = context.contentResolver.getType(uri)?.substringAfter("/")?:"img"
            val file = File(directory, "${folder.folderName}_$fileName.$mimeType")

            if(file.exists()) file.delete()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            return file.toUri() // Возвращаем сохранённый файл
        } catch (e: Exception) {
            e.printStackTrace()
            return null // Возвращаем null в случае ошибки
        }
    }
    fun loadBitmapFromFilesDir(context: Context, fileName: String):Bitmap?{
        val file = File(context.filesDir, fileName)

        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath) // Преобразуем файл обратно в Bitmap
        } else {
            null
        }
    }
}

enum class UserInfoStorageFolder(val folderName: String){
    USER_ICONS("UserIcons"),
    USER_BANNERS("UserBanners")
}