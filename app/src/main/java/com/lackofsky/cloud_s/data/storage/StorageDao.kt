package com.lackofsky.cloud_s.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.IOException

class StorageDao {
    fun saveProfileIcon(context: Context, bitmap: Bitmap, fileName: String): String? {
        val file = File(context.filesDir, fileName)

        return try {
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // Сохраняем изображение в формате PNG
            }
            file.absolutePath // Возвращаем путь к файлу
        } catch (e: IOException) {
            e.printStackTrace()
            null // Ошибка при сохранении
        }
    }

    fun loadProfileIcon(context: Context, fileName: String): Bitmap? {
        val file = File(context.filesDir, fileName)

        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath) // Преобразуем файл обратно в Bitmap
        } else {
            null // Файл не найден
        }
    }

    fun deleteProfileIcon(context: Context, fileName: String): Boolean {
        val file = File(context.filesDir, fileName)

        return if (file.exists()) {
            file.delete() // Удаляем файл
        } else {
            false // Файл не существует
        }
    }

}