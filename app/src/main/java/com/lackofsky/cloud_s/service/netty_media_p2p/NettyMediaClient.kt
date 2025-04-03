package com.lackofsky.cloud_s.service.netty_media_p2p

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.netty_media_p2p.model.MediaRequest
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMode
import io.netty.bootstrap.Bootstrap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets


class NettyMediaClient(
    val context: Context): MediaClientInterface {
    val TAG = "GrimBerry NettyMediaClient"

    override fun sendMessageFile(message: Message, sender: User,
        serverIpAddr: String, serverPort: Int): Boolean {
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

        override fun sendUserLogoFile(
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
                    transferMediaIntend = TransferMediaIntend.MEDIA_USER_LOGO
                )
                return sendFile(uri = uri, mediaRequest = mediaRequest,
                    serverIpAddr, serverPort)
            } ?: return false
        }

        override fun sendUserBannerFile(
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

    private fun getFileDetails(uri: Uri): FileDetails? {
        var fileName: String? = null
        var fileSize: Long? = null
        val mimeType: String? = context.contentResolver.getType(uri)

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            if (cursor.moveToFirst()) {
                fileName = if (nameIndex != -1) cursor.getString(nameIndex) else null
                fileSize = if (sizeIndex != -1) cursor.getLong(sizeIndex) else null
            }
        }
        try {
            return FileDetails(fileName!!, mimeType!!, fileSize!!)
        }catch (e: Exception){
            Log.d(TAG, "getFileDetails: $e. filename = ${fileName}, mimeType = ${mimeType}, fileSize = ${fileSize}")
            return null
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
                                .addLast(object : SimpleChannelInboundHandler<ByteArray>() {
                                    override fun channelActive(ctx: ChannelHandlerContext) {
                                        Log.d(
                                            TAG,
                                            "Подключено к серверу ${ctx.channel().remoteAddress()}"
                                        )
                                        sendMetadata(
                                            ctx,
                                            mediaRequest
                                        ) //сделать обработчик пайплайн. отправка реквеста, передача следующему обработчику
                                        sendFileData(
                                            ctx,
                                            uri
                                        )//сделать следующий обработчик пайплайн. разбиваем файл на куски в список строк и отправляем пользователю кусками установленной длинны.
                                        // так же данный обработчик должен будет принимать ответы сервера: который может кидать json chunk_request, chunkId={}, и будет отвечать ему нужным куском

                                    }

                                    /*** TODO remake this */
                                    override fun channelRead0(
                                        ctx: ChannelHandlerContext,
                                        msg: ByteArray
                                    ) {
                                        Log.d(TAG, "Ответ от сервера: ${String(msg)}")

                                        if (String(msg).startsWith("UPLOAD_SUCCESS")) {
                                            //TODO відповідь сервера
                                            ctx.close()
                                        }
                                    }

                                    override fun exceptionCaught(
                                        ctx: ChannelHandlerContext,
                                        cause: Throwable
                                    ) {
                                        Log.e(TAG, "Ошибка Netty клиента: ${cause.message}")
                                        ctx.close()
                                    }
                                })
                        }
                    })

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
            val metadataJson = Gson().toJson(mediaRequest)
            val buffer = Unpooled.wrappedBuffer(metadataJson.toByteArray(Charsets.UTF_8))
            ctx.writeAndFlush(buffer)
        }

        private fun sendFileData(ctx: ChannelHandlerContext, uri: Uri) {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e(TAG, "Не удалось открыть поток для $uri")
                ctx.close()
                return
            }
            inputStream.use { stream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(buffer, 0, bytesRead))
                }
            }
            Log.d(TAG, "Файл успешно отправлен: $uri")
            ctx.close()
        }
}


interface MediaClientInterface {
    fun sendMessageFile(message: Message, sender: User,serverIpAddr: String, serverPort:Int): Boolean //sender = userOwner
    fun sendUserLogoFile(uri: Uri, sender: User, serverIpAddr: String, serverPort:Int): Boolean
    fun sendUserBannerFile(uri: Uri, sender: User, serverIpAddr: String, serverPort:Int): Boolean
}

data class FileDetails(
    val name: String,
    val mimeType: String,
    val size: Long,
)