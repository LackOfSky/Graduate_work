package com.lackofsky.cloud_s.data.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import java.io.File

class StorageRepository(val metadata: Metadata) {
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



    // ====== Specific Types ======
     fun isMediaExist(fileName: String,fileIntend:TransferMediaIntend,fileSize: Long){

     }
//    fun saveMessageFile(context: Context, uri: Uri, fileName: String): Uri? {
//        val mimeType = context.contentResolver.getType(uri) ?: return null
//
//        return when {
//            mimeType.startsWith("image/") -> {
//                saveImageToGallery(context, uri, fileName)
//            }
//            mimeType.startsWith("video/") -> {
//                saveVideoToGallery(context, uri, fileName)
//            }
//            mimeType.startsWith("audio/") -> {
//                saveAudioToMusic(context, uri, fileName)
//            }
//            mimeType == "application/pdf" || mimeType.startsWith("text/") ||
//                    mimeType == "application/msword" || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
//                    mimeType == "application/vnd.ms-excel" || mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {
//                saveDocumentToDocuments(context, uri, fileName)
//            }
//            else -> {
//                saveFileToDownloads(context, uri, fileName)
//            }
//        }
//    }
    fun saveImageToGallery(context: Context, uri: Uri, fileName: String): Uri? {
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
        return saveToMediaStore(
            context,
            uri,
            fileName,
            mimeType,
            "Pictures/cLoud_s",
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        )
     }
    fun saveVideoToGallery(context: Context, uri: Uri, fileName: String): Uri? {
        val mimeType = context.contentResolver.getType(uri) ?: "video/mp4"
        return saveToMediaStore(
            context = context,
            sourceUri = uri,
            fileName = fileName,
            mimeType = mimeType,
            relativePath = "Movies/cLoud_s",
            mediaCollectionUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        )
    }

    fun saveAudioToMusic(context: Context, uri: Uri, fileName: String): Uri? {
        val mimeType = context.contentResolver.getType(uri) ?: "audio/mpeg"
        return saveToMediaStore(
            context,
            uri,
            fileName,
            mimeType,
            "Music",
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        )
    }

    fun saveDocumentToDocuments(context: Context, uri: Uri, fileName: String): Uri? {
        val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"
        return saveToMediaStore(
            context,
            uri,
            fileName,
            mimeType,
            "Documents",
            MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        )
    }

    fun saveFileToDownloads(context: Context, uri: Uri, fileName: String): Uri? {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        return saveToMediaStore(
            context,
            uri,
            fileName,
            mimeType,
            "Download",
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        )
    }
    private fun saveToMediaStore(
        context: Context,
        sourceUri: Uri,
        fileName: String,
        mimeType: String,
        relativePath: String,
        mediaCollectionUri: Uri
    ): Uri? {
        val resolver = context.contentResolver

        val extension = mimeType.substringAfter("/", "bin")
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val itemUri = resolver.insert(mediaCollectionUri, contentValues)

        return try {
            itemUri?.let { destUri ->
                resolver.openOutputStream(destUri)?.use { output ->
                    resolver.openInputStream(sourceUri)?.use { input ->
                        input.copyTo(output)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(destUri, contentValues, null, null)

                destUri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

enum class UserInfoStorageFolder(val folderName: String){
    USER_ICONS("UserIcons"),
    USER_BANNERS("UserBanners")
}