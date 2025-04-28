package com.lackofsky.cloud_s.service.netty_media_p2p

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.netty_media_p2p.model.MediaRequest
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMode
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import java.net.InetSocketAddress
import io.netty.handler.stream.ChunkedFile
import io.netty.util.CharsetUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets


class NettyMediaClient(
    val context: Context,
    val userRepository: UserRepository,
    val messageRepository: MessageRepository,
    val gson: Gson): MediaClientInterface {

    val TAG = "GrimBerry NettyMediaClient"
    val userOwner: Flow<User> = userRepository.getUserOwner()

    override suspend fun sendMessageFile(messageUniqueId: String, sender: User,
        serverIpAddr: String, serverPort: Int): Boolean {
        val message = messageRepository.getMessageByUniqueId(messageUniqueId).first()
        val uri = message.mediaUri!!.toUri()
        val fileDetails = getFileDetails(uri)
        fileDetails?.let {
            val mediaRequest = MediaRequest(
                fileName = fileDetails.name,
                messageId = message.uniqueId!!,
                mimeType = fileDetails.mimeType,
                fileSize = fileDetails.size,
                checksum = null,// TODO
                chunkSize = null, //TODO додавати в подальших хендлерах, в наступних версіях
                transferMode = TransferMode.MULTIPART, //todo
                senderId = sender.uniqueID,
                transferMediaIntend = TransferMediaIntend.MEDIA_EXTERNAL
            )
            return sendFile(uri = uri, mediaRequest = mediaRequest,
                serverIpAddr, serverPort)
        } ?: return false


    }

        override suspend fun sendUserLogoFile(
            uri: Uri, sender: User,
            serverIpAddr: String, serverPort: Int
        ): Boolean {
            val fileDetails = getFileDetails(uri)
            fileDetails?.let {
                    val mediaRequest = MediaRequest(
                        fileName = fileDetails.name,
                        mimeType = fileDetails.mimeType,
                        fileSize = fileDetails.size,
                        checksum = null,// TODO
                        chunkSize = null, //TODO додавати в подальших хендлерах, в наступних версіях
                        transferMode = TransferMode.MULTIPART, //todo
                        senderId = userRepository.getUserOwner().first().uniqueID,//sender.uniqueID,
                        transferMediaIntend = TransferMediaIntend.MEDIA_USER_LOGO
                    )
                Log.d(TAG, "sendUserLogoFile fileDetails.name: ${fileDetails.name}")
                return sendFile(uri = uri, mediaRequest = mediaRequest,
                    serverIpAddr, serverPort)
            } ?: return false
        }

        override suspend fun sendUserBannerFile(
            uri: Uri, sender: User,
            serverIpAddr: String, serverPort: Int
        ): Boolean {
            val fileDetails = getFileDetails(uri)
            fileDetails?.let {
                val mediaRequest = MediaRequest(
                    fileName = fileDetails.name,
                    mimeType = fileDetails.mimeType,
                    fileSize = fileDetails.size,
                    checksum = null,// TODO
                    chunkSize = null, //TODO додавати в подальших хендлерах, в наступних версіях
                    transferMode = TransferMode.MULTIPART, //todo
                    senderId = sender.uniqueID,
                    transferMediaIntend = TransferMediaIntend.MEDIA_USER_BANNER
                )
                return sendFile(uri = uri, mediaRequest = mediaRequest,
                    serverIpAddr, serverPort)
            } ?: return false
        }

//    private fun getFileDetails(uri: Uri): FileDetails? {
//        var fileName: String? = null
//        var fileSize: Long? = null
//        val mimeType: String? = context.contentResolver.getType(uri)
//
//        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
//            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
//
//            if (cursor.moveToFirst()) {
//                fileName = if (nameIndex != -1) cursor.getString(nameIndex) else null
//                fileSize = if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
//            }
//        }
//        Log.d(TAG, "getFileDetails: $fileName, $mimeType, $fileSize")
//        try {
//            return FileDetails(fileName!!, mimeType!!, fileSize!!)
//        }catch (e: Exception){
//            Log.d(TAG, "getFileDetails: $e. filename = ${fileName}, mimeType = ${mimeType}, fileSize = ${fileSize}")
//            return null
//        }
//
//    }
private fun getFileDetails(uri: Uri): FileDetails? {
    val contentResolver = context.contentResolver
    val scheme = uri.scheme

    var fileName: String? = null
    var fileSize: Long? = null
    var mimeType: String? = null

    return try {
        when (scheme) {
            "content" -> {
                mimeType = contentResolver.getType(uri)
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        fileName = if (nameIndex != -1) cursor.getString(nameIndex) else null
                        fileSize = if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
                    }
                }
            }
            "file" -> {
                val file = File(uri.path!!)
                fileName = file.name
                fileSize = file.length()
                mimeType = contentResolver.getType(uri) ?: guessMimeType(file)
            }
        }

        if (fileName != null && fileSize != null && mimeType != null) {
            FileDetails(fileName!!, mimeType, fileSize!!)
        } else {
            Log.d(TAG, "getFileDetails: filename = ${fileName}, mimeType = ${mimeType}, fileSize = ${fileSize}")
            Log.d(TAG, "getFileDetails: some values are null -> fileName=$fileName, mimeType=$mimeType, fileSize=$fileSize")
            null
        }

    } catch (e: Exception) {
        Log.e(TAG, "getFileDetails error: $e")
        Log.d(TAG, "getFileDetails: $e. filename = ${fileName}, mimeType = ${mimeType}, fileSize = ${fileSize}")
        null
    }
}
    private fun guessMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
        /*** */
        private fun sendFile(
            uri: Uri, mediaRequest: MediaRequest,
            serverIpAddr: String, serverPort: Int
        ): Boolean {
            val group = NioEventLoopGroup()
            return try {
                val bootstrap = Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel::class.java)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(object : ChannelInitializer<Channel>() {
                        override fun initChannel(ch: Channel) {
                            ch.pipeline()
                                .addLast(object : SimpleChannelInboundHandler<ByteBuf>() {
                                    override fun channelActive(ctx: ChannelHandlerContext) {
                                        Log.d(
                                            TAG,
                                            "Подключено к серверу ${ctx.channel().remoteAddress()}"
                                        )
                                        Log.d(TAG, "sendMetadata ${mediaRequest}")
                                        sendMetadata(
                                            ctx,
                                            mediaRequest
                                        ) //сделать обработчик пайплайн. отправка реквеста, передача следующему обработчику

                                        sendFileData(
                                            ctx,
                                            uri
                                        )//сделать следующий обработчик пайплайн. разбиваем файл на куски в список строк и отправляем пользователю кусками установленной длинны.
                                        // так же данный обработчик должен будет принимать ответы сервера: который может кидать json chunk_request, chunkId={}, и будет отвечать ему нужным куском
                                        val bufEnd = Unpooled.buffer()
                                        bufEnd.writeByte(0x03) // Конец
                                        bufEnd.writeInt(0)
                                        ctx.writeAndFlush(bufEnd)
                                        Log.d("MediaClient", "FILE_TRANSFER_COMPLETE.")
                                    }



                                    /*** TODO remake this */
                                    override fun channelRead0(ctx: ChannelHandlerContext, msg: ByteBuf) {
                                        val response = msg.toString(CharsetUtil.UTF_8).trim()
                                        Log.d(TAG, "Ответ от сервера: $response")
                                        if (response == "UPLOAD_SUCCESS") {
                                            ctx.close()
                                        }
                                    }

                                    override fun exceptionCaught(
                                        ctx: ChannelHandlerContext,
                                        cause: Throwable
                                    ) {
                                        Log.e(TAG, "Ошибка Netty клиента: ${cause.message}", cause)
                                        ctx.close()
                                    }
                                })
                        }
                    })
                Log.d(TAG, "Подключение к серверу... ${serverIpAddr}  ${serverPort}")
                val channel = bootstrap.connect(serverIpAddr, serverPort).sync().channel()
                channel.closeFuture().sync()
                true
            } catch (e: Exception) {

                Log.e(TAG, "Ошибка отправки файла: ${e.message}")
                false
            } finally {
                group.shutdownGracefully()
            }
        }

        /*** */
        private fun sendMetadata(ctx: ChannelHandlerContext, mediaRequest: MediaRequest) {
            val metadataJsonBytes = gson.toJson(mediaRequest).toByteArray()
            val bufJson = Unpooled.buffer()
            bufJson.writeByte(0x01) // JSON
            bufJson.writeInt(metadataJsonBytes.size)
            bufJson.writeBytes(metadataJsonBytes)
            ctx.writeAndFlush(bufJson)
        }

        private fun sendFileData(ctx: ChannelHandlerContext, uri: Uri) {
            context.contentResolver.openInputStream(uri)?.use { input ->

                val buffer = ByteArray(8192)
                var chunkId = 0
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    // Строим заголовок для текущего чанка
                    val bufFile = Unpooled.buffer()
                    bufFile.writeByte(0x02) // Файл

                    val chunkSize = bytesRead
                    bufFile.writeInt(chunkSize)
                    bufFile.writeBytes(buffer.copyOf(bytesRead))
                    ctx.writeAndFlush(bufFile)
                    chunkId++
                    Log.d(TAG, "Отправка чанка: $chunkId, данные: ${buffer.copyOf(bytesRead)}")
                }

            }?: Log.e(TAG, "Не удалось открыть поток для $uri")
            Log.d(TAG, "Файл успешно отправлен: $uri")
        }

}


interface MediaClientInterface {
    suspend fun sendMessageFile(messageUniqueId: String, sender: User,serverIpAddr: String, serverPort:Int): Boolean //sender = userOwner
    suspend fun sendUserLogoFile(uri: Uri,sender: User,  serverIpAddr: String, serverPort:Int): Boolean //
    suspend fun sendUserBannerFile(uri: Uri, sender: User, serverIpAddr: String, serverPort:Int): Boolean
}

data class FileDetails(
    val name: String,
    val mimeType: String,
    val size: Long,
)