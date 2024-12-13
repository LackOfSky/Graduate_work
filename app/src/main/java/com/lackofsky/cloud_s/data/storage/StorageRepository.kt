package com.lackofsky.cloud_s.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

class StorageRepository {
    fun saveFileFromUri(context: Context, uri: Uri, fileName: String): File? {
        try {
            // Создаём файл в приватной директории приложения
            val file = File(context.filesDir, fileName)

            // Открываем InputStream для чтения данных из URI
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Открываем OutputStream для записи данных в файл
                file.outputStream().use { outputStream ->
                    // Копируем данные из InputStream в OutputStream
                    inputStream.copyTo(outputStream)
                }
            }

            return file // Возвращаем сохранённый файл
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