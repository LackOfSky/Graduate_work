package com.lackofsky.cloud_s.service.server.handlers

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture


/*** handler для роботи з повідомленнями між користувачами
 *
 *
 * фактично непрацездатний. логіка знову повертається в NettyServerHandler
 * */
class MessageHandler(private val messageRepository: MessageRepository,
                     private val userRepository: UserRepository,
                     private val chatRepository: ChatRepository,
                     private val clientPartP2P: ClientPartP2P,
    //private val friendResponseUseCase: FriendResponseUseCase
) : SimpleChannelInboundHandler<String>() {
    val gson = GsonBuilder()
        .setDateFormat("MMM dd, yyyy HH:mm:ss") // Указываем формат даты
        .create()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String?) {
            try {
                val data = gson.fromJson(msg, TransportData::class.java)
                Log.d("service $SERVICE_NAME message handler", "received: $data")

//                if (
                    processMessage(ctx, data, msg)
//                    ) {
//
//                } else {
//                    ctx.fireChannelRead(msg)
//                }

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

    /*** return type для визначення необхідності надавати данні в наступний pipeline (true to stop) */
    private fun processMessage(ctx: ChannelHandlerContext,data: TransportData,msg: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            when (data.messageType) {
                MessageType.MESSAGE -> {
                    val message = gson.fromJson(data.content, Message::class.java)
                    Log.d("service $SERVICE_NAME server handler", " mess age " + message.toString())
                    //try {

//                            chatRepository.getAllChats().collect { chats ->
//                                Log.d(
//                                    "service $SERVICE_NAME server handler",
//                                    chats.toString()
//                                )
//                            }
                    chatRepository.getChatById(message.chatId).collect { chat ->
                        Log.d(
                            "service $SERVICE_NAME server handler",
                            chat.toString()
                        )
                    }
                    try {

                        GlobalScope.launch(Dispatchers.IO) {
                            messageRepository.insertMessage(message)
                            withContext(Dispatchers.Main){
                                ctx.flush()
                            }
                        }

                    }catch (e:Exception){
                        Log.d("service $SERVICE_NAME message handler", e.toString())
                    }
                    messageRepository.getMessageByUniqueId(message.uniqueId!!).collect {
                        Log.d("service $SERVICE_NAME server handler", "mess " + it.toString())
                    }
                    messageRepository.getMessagesByChat(message.chatId).collect {
                        Log.d("service $SERVICE_NAME server handler", "mess " + it.toString())
                    }


                    // messageRepository.insertMessage(message)
                    //} catch (e: Exception) {
                    //Log.d("service $SERVICE_NAME server handler", e.toString())
                    //}
                    Log.d("service $SERVICE_NAME server handler", "message added")

                }

                MessageType.MESSAGE_EDIT -> {
                    TODO()
                }

                MessageType.MESSAGE_DELETE -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        val message = gson.fromJson(data.content, Message::class.java)
                    }
                    TODO()
                }

                else -> {
                    ctx.fireChannelRead(msg)
                }
            }
        }
        Log.d(
            "service $SERVICE_NAME message handler",
            "msg absorbed by message handler"
        )
        ctx.flush()
        //ctx.fireChannelReadComplete()

    }
}
