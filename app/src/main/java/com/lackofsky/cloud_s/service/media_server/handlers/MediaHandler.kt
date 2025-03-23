package com.lackofsky.cloud_s.service.media_server.handlers

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.storage.UserInfoStorageFolder
import com.lackofsky.cloud_s.service.media_server.model.MediaRequest
import com.lackofsky.cloud_s.service.media_server.model.TransferMediaIntend
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.ReferenceCountUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.channels.FileChannel


class MediaHandler(private val context: Context,
                   private val userRepository: UserRepository,
                   private val messageRepository: MessageRepository
) : SimpleChannelInboundHandler<ByteBuf>() {
    private val TAG = "GrimBerry mediahandler NMserver"
    private val APPLICATION_NAME = "cLoud_s"

    private var outputStream: OutputStream? = null
    private var fileUri: Uri? = null

    private var totalBytesReceived: Long = 0
    private var isReceivingFile = false
    private var mediaRequest: MediaRequest? = null
    private var isTransferSuccess = true

    override fun channelActive(ctx: ChannelHandlerContext) {
        isTransferSuccess = true
        totalBytesReceived = 0
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
        val bytes = ByteArray(msg.readableBytes())
        msg.readBytes(bytes)

        try {
            if (!isReceivingFile) {
                //Першим повідомленням отримуємо метаданні до файлу
                mediaRequest =
                    Gson().fromJson(msg.toString(Charsets.UTF_8), MediaRequest::class.java)
                Log.d(TAG, "Отримано метадані: ${mediaRequest}")

                // Генеруємо шлях до файлу у MediaStore

                when (mediaRequest!!.transferMediaIntend) {
                    TransferMediaIntend.MEDIA_USER_LOGO, TransferMediaIntend.MEDIA_USER_BANNER -> {
                        fileUri = saveToInternalStore(mediaRequest!!)
                    }

                    TransferMediaIntend.MEDIA_EXTERNAL -> {
                        fileUri = saveToMediaStore(mediaRequest!!)
                    }
                }

                fileUri?.let { uri ->
                    outputStream = context.contentResolver.openOutputStream(uri)
                }
                isReceivingFile = true
            } else {
                // Якщо отримуємо файл, пишемо його у OutputStream
                outputStream?.let { stream ->
                    msg.readBytes(bytes)
                    stream.write(bytes)
                    totalBytesReceived += bytes.size
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "nettyMediaServer media handler error: " + e.printStackTrace().toString())
            ctx.close()
            outputStream?.close()
            fileUri?.let { uri ->
                context.contentResolver.delete(uri, null, null)
            }
            isReceivingFile = false
            totalBytesReceived = 0L
            fileUri = null
            isTransferSuccess = false
        } finally {
            ReferenceCountUtil.release(msg)
        }
        // Обробка отриманих даних у фоновому потоці
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        outputStream?.close()
        Log.d(TAG, "Файл отримано: $fileUri, Розмір: $totalBytesReceived байтів")
        if (isTransferSuccess) {
            //перевірка перед доданням до бд: mediaRequest.checksum
            mediaRequest!!.let { request ->
                CoroutineScope(Dispatchers.IO).launch {
                    when (request.transferMediaIntend) {
                        TransferMediaIntend.MEDIA_USER_LOGO -> {
                            val userInfo = userRepository.getUserInfoById(request.senderId).first()
                            userRepository.updateUserInfo(userInfo.copy(iconImgURI = fileUri.toString()))
                            Log.d(TAG, "user ${request.senderId} logo updated ")
                        }

                        TransferMediaIntend.MEDIA_USER_BANNER -> {
                            val userInfo = userRepository.getUserInfoById(request.senderId).first()
                            userRepository.updateUserInfo(userInfo.copy(bannerImgURI = fileUri.toString()))
                            Log.d(TAG, "user ${request.senderId} banner updated to $fileUri")
                        }

                        TransferMediaIntend.MEDIA_EXTERNAL -> {
                            val message =
                                messageRepository.getMessageByUniqueId(request.messageId).first()
                            messageRepository.insertMessage(message.copy())
                            Log.d(
                                TAG,
                                "user ${request.senderId} message media added. messageId: ${request.messageId}"
                            )
                        }
                    }
                }
            }
            ctx.writeAndFlush("UPLOAD_SUCCESS:${mediaRequest!!.fileName} \n")
        }

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Log.e(TAG, "nettyMediaServer media handler error: " + cause.printStackTrace().toString())
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
        try {
            FileOutputStream(file).also { outputStream = it }
        } catch (e: Exception) {
            Log.e(TAG, "Помилка збереження логотипу: ${e.message}")
            return null
        }
        return file.toUri()
    }
}