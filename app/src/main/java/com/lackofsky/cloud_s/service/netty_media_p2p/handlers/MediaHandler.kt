package com.lackofsky.cloud_s.service.netty_media_p2p.handlers

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.storage.StorageRepository
import com.lackofsky.cloud_s.data.storage.UserInfoStorageFolder
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaServer
import com.lackofsky.cloud_s.service.netty_media_p2p.model.MediaRequest
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.CharsetUtil
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class MediaHandler(private val context: Context,
                   private val userRepository: UserRepository,
                   private val messageRepository: MessageRepository,
                   private val storageRepository: StorageRepository,
    private val gson: Gson
) : SimpleChannelInboundHandler<Any>() {
    private val TAG = "GrimBerry mediahandler NMserver"
    private val APPLICATION_NAME = "cLoud_s"

    private var outputStream: OutputStream? = null
    private var fileUri: Uri? = null
    private var currentFile: File? = null

    private var totalBytesReceived: Long = 0
    private var mediaRequest: MediaRequest? = null
    //private var isTransferSuccess = true

    override fun channelActive(ctx: ChannelHandlerContext) {
        //isTransferSuccess = true
        totalBytesReceived = 0
        mediaRequest  = null
        outputStream = null
        fileUri = null

    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any) {
        try {
            when(msg){
                is MediaRequest->{
                    Log.d(TAG, "Отримано метадані: $msg")
                    mediaRequest = msg

                    fileUri = when (msg.transferMediaIntend) {
                        TransferMediaIntend.MEDIA_USER_LOGO,
                        TransferMediaIntend.MEDIA_USER_BANNER -> saveToInternalStore(msg)
                        TransferMediaIntend.MEDIA_EXTERNAL -> saveToMediaStore(msg)
                    }


                    fileUri?.let { uri ->
                        outputStream = context.contentResolver.openOutputStream(uri)
                    } ?: throw Exception("Не вдалося отримати URI для збереження файлу")

                }
                is ByteArray -> {
//                    val buffer = ByteArray(msg.readableBytes())
//                    msg.readBytes(buffer)
                    outputStream?.write(msg)
                    outputStream?.flush()
                    totalBytesReceived += msg.size
//                    Log.d(TAG, "Отримано дані: ${msg.size}")
                }
                is String->{
                    if (msg.trim() == "FILE_TRANSFER_COMPLETE") {
                        Log.d(TAG, "Передача завершена.")
                        // Finalize transfer
                        outputStream?.close()
                        outputStream = null
                        // Update DB asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                                Log.d(TAG, "Файл отримано: $fileUri, Розмір: $totalBytesReceived байтів")
                                mediaRequest?.let { request ->
                                    val userInfo = userRepository.getUserInfoById(request.senderId).first()
                                    when(request.transferMediaIntend){
                                        TransferMediaIntend.MEDIA_USER_LOGO ->{
                                            userRepository.updateUserInfo(userInfo.copy(iconImgURI = fileUri.toString()))
                                            Log.d(TAG, "user ${request.senderId} logo updated to $fileUri")
                                        }
                                        TransferMediaIntend.MEDIA_USER_BANNER -> {
                                            userRepository.updateUserInfo(userInfo.copy(bannerImgURI = fileUri.toString()))
                                            Log.d(TAG, "user ${request.senderId} banner updated to $fileUri")
                                        }
                                        TransferMediaIntend.MEDIA_EXTERNAL -> {
                                            val message = messageRepository.getMessageByUniqueId(request.messageId).first()
                                            /*TODO*/ //update message by id
                                            messageRepository.insertMessage(message)
                                            Log.d(TAG, "user ${request.senderId} message media added. messageId: ${request.messageId}"
                                            )
                                        }
                                    }

                                }?:throw Exception("FILE_TRANSFER_COMPLETE. mediaRequest is null")
                            //}
                        }

                        ctx.close()
                    }else{
                        Log.d(TAG, "Помилка обробки повідомлення: ${msg.trim()} message type is string bun not \"FILE_TRANSFER_COMPLETE\"")
                        throw Exception("FILE_TRANSFER_COMPLETE. message type is string but not \"FILE_TRANSFER_COMPLETE\"")
                    }
                }
                else->{
                    Log.e(TAG, "nettyMediaServer media handler error: ${msg.toString()}")
                    throw Exception("nettyMediaServer media handler error: ${msg.toString()}")
                }
            }
        }catch (e:Exception){
            Log.e(TAG, "Помилка обробки повідомлення: ${e.message}", e)
            cleanupOnError()
            ctx.close()
        }
    }
    private fun cleanupOnError() {
        try {
            outputStream?.close()
        } catch (ex: IOException) {
            Log.e("MediaHandler NMS", "Failed to close stream: ${ex.message}", ex)
        }
        mediaRequest = null
        totalBytesReceived = 0
        outputStream = null
        fileUri?.let { context.contentResolver.delete(it, null, null) }   // Remove incomplete MediaStore entry
        currentFile?.let { if (it.exists()) it.delete() }
        fileUri = null
        currentFile = null

    }
    override fun channelInactive(ctx: ChannelHandlerContext) {
        cleanupOnError()
        super.channelInactive(ctx)
//        outputStream?.close()
//        Log.d(TAG, "Файл отримано: $fileUri, Розмір: $totalBytesReceived байтів")
//        CoroutineScope(Dispatchers.IO).launch {
//        if (isTransferSuccess && fileUri != null) {
//            //перевірка перед доданням до бд: mediaRequest.checksum
//                try{
//                    Log.d(TAG, "Файл отримано: трансфер $isTransferSuccess  ")
//                    mediaRequest?.let { request ->
//                        when (request.transferMediaIntend) {
//                            TransferMediaIntend.MEDIA_USER_LOGO -> {
//                                Log.d(TAG, "user ${request.senderId}")
//
//                                val userInfo = userRepository.getUserInfoById(request.senderId)
//                                userRepository.getUserInfoById(request.senderId)
//                                Log.d(TAG, "user ${request.senderId} ${userInfo.first().userId}")
//                                userRepository.updateUserInfo(userInfo.first().copy(iconImgURI = fileUri.toString()))
//                                Log.d(TAG, "user ${request.senderId} logo updated ")
//                                Log.d(TAG, "user updated: ${userRepository.getUserInfoById(request.senderId)}")
//                            }
//                            TransferMediaIntend.MEDIA_USER_BANNER -> {
//                                val userInfo = userRepository.getUserInfoById(request.senderId).first()
//                                userRepository.updateUserInfo(userInfo.copy(bannerImgURI = fileUri.toString()))
//                                Log.d(TAG, "user ${request.senderId} banner updated to $fileUri")
//                            }
//                            TransferMediaIntend.MEDIA_EXTERNAL -> {
//                                val message =
//                                    messageRepository.getMessageByUniqueId(request.messageId).first()
//                                messageRepository.insertMessage(message.copy(mediaUri = fileUri.toString()))
//                                Log.d(
//                                    TAG,
//                                    "user ${request.senderId} message media added. messageId: ${request.messageId}"
//                                )
//                            }
//                        }
//                    }
//
//                    //ctx.writeAndFlush("UPLOAD_SUCCESS:${mediaRequest!!.fileName} \n")
//                }catch (e: Exception) {
//                    Log.e(TAG, "Помилка при оновленні БД: ${e.message}", e)
//                }
//
//            }
//            ctx.close()
//            outputStream = null
//            fileUri = null
//            mediaRequest = null
//            totalBytesReceived = 0
//            isTransferSuccess = false
//        }

    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Log.e(TAG, "nettyMediaServer media handler error: " + cause.printStackTrace().toString())
        cleanupOnError()
        ctx.close()
    }


    private fun saveToMediaStore(mediaRequest: MediaRequest): Uri? {
        val contentValues = ContentValues()
        mediaRequest.mimeType.let { mimeType ->
            val fileExtension = mimeType.substringAfter("/")
            when {
                mimeType.startsWith("image/") -> {
                    contentValues.apply {
                        mediaRequest.fileName?.let {
                            put(MediaStore.Images.Media.DISPLAY_NAME, it)
                        }
                            ?: put(
                                MediaStore.Images.Media.DISPLAY_NAME,
                                "received_${System.currentTimeMillis()}.${fileExtension}"
                            )
                        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$APPLICATION_NAME")
                    }
                    return context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                }

                mimeType.startsWith("video/") -> {
                    contentValues.apply {
                        mediaRequest.fileName?.let {
                            put(MediaStore.Video.Media.DISPLAY_NAME, it)
                        }
                            ?: put(
                                MediaStore.Video.Media.DISPLAY_NAME,
                                "received_${System.currentTimeMillis()}.${fileExtension}"
                            )
                        put(MediaStore.Video.Media.MIME_TYPE, mediaRequest.mimeType)
                        put(
                            MediaStore.Video.Media.RELATIVE_PATH,
                            "Movies/${APPLICATION_NAME}"
                        ) // Папка "Movies/ReceivedMedia"
                    }
                    return context.contentResolver.insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                }

                mimeType.startsWith("audio/") -> {
                    contentValues.apply {
                        mediaRequest.fileName?.let {
                            put(MediaStore.Audio.Media.DISPLAY_NAME, it)
                        }
                            ?: put(
                                MediaStore.Audio.Media.DISPLAY_NAME,
                                "received_${System.currentTimeMillis()}.${fileExtension}"
                            )
                        put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                        put(
                            MediaStore.Audio.Media.RELATIVE_PATH,
                            "Music/$APPLICATION_NAME"
                        ) // Папка "Music/{app_name}"
                    }
                    return context.contentResolver.insert(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                }

                else -> {
                    contentValues.apply {
                        mediaRequest.fileName?.let {
                            put(MediaStore.Downloads.DISPLAY_NAME, it)
                        }
                            ?: put(
                                MediaStore.Downloads.DISPLAY_NAME,
                                "received_${System.currentTimeMillis()}.${fileExtension}"
                            )
                        put(MediaStore.Downloads.MIME_TYPE, mimeType)
                        put(MediaStore.Downloads.RELATIVE_PATH, "Download/$APPLICATION_NAME")
                    }
                    return context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                }
            }
        }


    }

    private fun saveToInternalStore(mediaRequest: MediaRequest): Uri? {
        val directory: File?
        when (mediaRequest.transferMediaIntend) {
            TransferMediaIntend.MEDIA_USER_LOGO -> {
                directory = File(context.filesDir, UserInfoStorageFolder.USER_ICONS.folderName).apply { mkdirs() }
            }

            TransferMediaIntend.MEDIA_USER_BANNER -> {
                directory = File(context.filesDir, UserInfoStorageFolder.USER_BANNERS.folderName).apply { mkdirs() }
            }

            TransferMediaIntend.MEDIA_EXTERNAL -> throw IllegalArgumentException("MediaHandler. saveToInternalStorage. MediaRequest.transferMediaIntend must be TransferMediaIntend.MEDIA_USER_LOGO or TransferMediaIntend.MEDIA_USER_BANNER")
        }
        val mimeType: String = mediaRequest.mimeType.substringAfter("/")
        val file =
            File(directory, mediaRequest.fileName ?: "user_icon_${System.currentTimeMillis()}.${mimeType}")
        currentFile = file
        Log.d(TAG, "mediaRequest.fileName: ${mediaRequest.fileName}, ${file.name}")
        try {
            FileOutputStream(file).also { outputStream = it }
        } catch (e: Exception) {
            Log.e(TAG, "Помилка збереження логотипу: ${e.message}")
            return null
        }
        return file.toUri()
    }
}