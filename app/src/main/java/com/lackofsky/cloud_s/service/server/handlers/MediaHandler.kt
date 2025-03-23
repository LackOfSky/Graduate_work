package com.lackofsky.cloud_s.service.server.handlers

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.server.MediaDispatcher
import com.lackofsky.cloud_s.service.server.MediaResponse
import com.lackofsky.cloud_s.service.server.MediaResponseStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class MediaHandler (private val messageRepository: MessageRepository,
                    private val userRepository: UserRepository,
                    private val chatRepository: ChatRepository,
                    private val clientPartP2P: ClientPartP2P,
                    private val mediaDispatcher: MediaDispatcher
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
                    if (mediaDispatcher.requestTransfer(data.senderId)) {
                        // Дозволяємо передачу та перенаправляємо запит до медіасервера
                        responseAccept(ctx)
                    } else {
                        // Відправляємо клієнту повідомлення "Зачекайте"
                        responseQueueted(ctx)
                    }
            }
            else ->{
                ctx.fireChannelRead(msg)
            }
        }

    }
    private fun responseAccept(ctx: ChannelHandlerContext) {
        // Тут логіка перекидання запиту на медіасервер (наприклад, через інший Netty-клієнт)
        ctx.writeAndFlush(MediaResponse(
            MediaResponseStatus.ACCEPTED,
            mediaDispatcher.mediaServerAddress!!,
            mediaDispatcher.mediaServerPort!! )
        )
    }
    private fun responseQueueted(ctx: ChannelHandlerContext){
        ctx.writeAndFlush(MediaResponse(
            MediaResponseStatus.QUEUETED,null,null
        ) )
    }
}

