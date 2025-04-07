package com.lackofsky.cloud_s.service.server.handlers

import android.util.Log
import androidx.core.net.toUri
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.ResponseData
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaClient
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaServer
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import com.lackofsky.cloud_s.service.server.MediaDispatcher
import com.lackofsky.cloud_s.service.server.MediaRequest
import com.lackofsky.cloud_s.service.server.MediaResponse
import com.lackofsky.cloud_s.service.server.MediaResponseStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaHandler (private val userRepository: UserRepository,
                    private val mediaDispatcher: MediaDispatcher,
                    private val mediaClient: NettyMediaClient,
                    private val clientPartP2P: ClientPartP2P,
                    private val mediaServer: NettyMediaServer
//private val friendResponseUseCase: FriendResponseUseCase
) : SimpleChannelInboundHandler<String>() {
    val gson = GsonBuilder()
        .setDateFormat("MMM dd, yyyy HH:mm:ss") // Указываем формат даты
        .create()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String?) {
        try {
            processMessage(ctx, msg!!)
        } catch (e: JsonParseException) {
            Log.e(
                "service $SERVICE_NAME message handler",
                "Error parsing message. non typical message: " + msg
            )
        } catch (e: IllegalStateException) {
            Log.e("service $SERVICE_NAME message handler", "Error json syntax message: $msg")
        } catch (_: Exception) {
            Log.e(
                "service $SERVICE_NAME message handler",
                "received unknown type of message:" + msg
            )
        }

    }
    private fun processMessage(ctx: ChannelHandlerContext, msg: String) {
        val data = gson.fromJson(msg, TransportData::class.java)
        Log.d("service $SERVICE_NAME message handler", "received: $data")
        when (data.messageType) {
            MessageType.REQUEST_MEDIA_SERVER -> {
                val request = gson.fromJson(data.content, MediaRequest::class.java)
                    if (mediaDispatcher.requestTransfer(data.senderId)) {

                        // Дозволяємо передачу та перенаправляємо запит до медіасервера
                        responseAccept(ctx, request)
                    } else {
                        // Відправляємо клієнту повідомлення "Зачекайте"
                        responseQueueted(ctx, request)
                    }
            }
            MessageType.RESPONSE_MEDIA_SERVER -> {
                // Отримуємо відповідь від медіасервера
                val incomingResponse = gson.fromJson(data.content, MediaResponse::class.java)
                if (incomingResponse.status == MediaResponseStatus.ACCEPTED) {
                    // Медіасервер прийняв запит на передачу

                    when(incomingResponse.requestedIntend){
                        TransferMediaIntend.MEDIA_USER_LOGO -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                mediaClient.sendUserLogoFile(clientPartP2P.userInfo.value!!.iconImgURI!!.toUri(),
                                    clientPartP2P.userOwner.value!!,
                                    incomingResponse.msIpAddress!!, incomingResponse.msPort!!)
                            }

                        }
                        TransferMediaIntend.MEDIA_USER_BANNER -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                mediaClient.sendUserBannerFile(clientPartP2P.userInfo.value!!.bannerImgURI!!.toUri(),
                                    clientPartP2P.userOwner.value!!,
                                    incomingResponse.msIpAddress!!, incomingResponse.msPort!!)
                            }
                        }
                        TransferMediaIntend.MEDIA_EXTERNAL -> TODO()
                        null -> TODO()
                    }

                    // Тут логіка передачіфайлу через мережу (наприклад, через SocketChannel)
                }
            }
            else ->{
                ctx.fireChannelRead(msg)
            }
        }

    }
    private fun responseAccept(ctx: ChannelHandlerContext,response: MediaRequest) {
        // Тут логіка перекидання запиту на медіасервер (наприклад, через інший Netty-клієнт)
        //todo вІдправляти серверу, а не клієнту
        //todo скоротити логіку респонсів (accept, queueted)
        CoroutineScope(Dispatchers.IO).launch {
            val sendTo = clientPartP2P.activeFriends.value.entries.find { it.key.uniqueID == response.userUniqueId }
            sendTo?.let {
                val sender = gson.toJson( clientPartP2P.userOwner.value!! )
                val responseJson = gson.toJson(
                    MediaResponse(
                        MediaResponseStatus.ACCEPTED,
                        mediaDispatcher.mediaServerAddress!!,
                        mediaDispatcher.mediaServerPort!!,
                        response.requestedIntend,
                        response.messageId)
                )

                val transportData = gson.toJson(TransportData(
                    messageType = MessageType.RESPONSE_MEDIA_SERVER,
                    senderId = clientPartP2P.userOwner.value!!.uniqueID,
                    sender = sender,
                    content = responseJson
                    )
                )
                val json = gson.toJson(transportData)

                sendTo.value.sendMessage(json)
            }?: Log.e("GrimBerry MediaHandler","responseAccept error: sendTo is null")


        }


       // response(ctx, responseJson)
    }
    private fun responseQueueted(ctx: ChannelHandlerContext,response: MediaRequest){
        CoroutineScope(Dispatchers.IO).launch {
            val sendTo = clientPartP2P.activeFriends.value.entries.find { it.key.uniqueID == response.userUniqueId }
            sendTo?.let {
                val sender = gson.toJson(clientPartP2P.userOwner.value!!)
                val responseJson = gson.toJson(MediaResponse(
                        MediaResponseStatus.QUEUETED,null,null, null, null
                    )
                )
                val transportData = gson.toJson(TransportData(
                    messageType = MessageType.RESPONSE_MEDIA_SERVER,
                    senderId = clientPartP2P.userOwner.value!!.uniqueID,
                    sender = sender,
                    content = responseJson
                )
                )
                val json = gson.toJson(transportData)

                sendTo.value.sendMessage(json)
            }?: Log.e("GrimBerry MediaHandler","responseQueueted error: sendTo is null")
        }
        //response(ctx, responseJson)
    }

}

