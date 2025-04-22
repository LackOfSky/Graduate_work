package com.lackofsky.cloud_s.service.server

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.MessageKey
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.model.Request
import com.lackofsky.cloud_s.service.model.TransportData
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class NettyServerHandler(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val clientPartP2P: ClientPartP2P,
    //private val friendResponseUseCase: FriendResponseUseCase
) : SimpleChannelInboundHandler<String>() {
    val gson = GsonBuilder()
        .setDateFormat("MMM dd, yyyy HH:mm:ss") // Указываем формат даты
        .create()


    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        try {
            val data = gson.fromJson(msg, TransportData::class.java)
            Log.d("service $SERVICE_NAME server handler", "received: $data")
            processMessage(ctx, data)
        } catch (e: JsonParseException) {
            Log.e("service GrimBerry SH" + " NettyServerHandler",
                "Error parsing message. non typical message: " + msg )
        } catch (e: IllegalStateException) {
            Log.e("service GrimBerry SH" + " NettyServerHandler", "Error json syntax message: $msg")
            Log.d("service GrimBerry SH", "non typical message: " + msg)
        } catch (_: Exception) {
            Log.e("service GrimBerry SH", "received unknown type of message:" + msg)
        }

//        ctx.writeAndFlush(
//            "Message received from " + ctx.channel().remoteAddress()
//        )//TODO обработка логики подтверждения приема сообщенияс

    }

    private fun processMessage(ctx: ChannelHandlerContext, data: TransportData) {
        // Здесь можно обработать сообщение и взаимодействовать с messageRepository
        CoroutineScope(Dispatchers.IO).launch {
            val remoteIpAddress = ctx.pipeline().channel().remoteAddress()

            when (data.messageType) {
                MessageType.MESSAGE -> {
                    val message = gson.fromJson(data.content, Message::class.java)
                    Log.d("service $SERVICE_NAME server handler", message.toString())
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            chatRepository.getAllChats().collect { chats ->Log.d("service $SERVICE_NAME server handler", chats.toString())}
                            chatRepository.getChatById(message.chatId).collect{chat ->Log.d("service $SERVICE_NAME server handler", chat.toString())}
                        }
                        messageRepository.insertMessage(message)
                    }catch (e:Exception){
                        Log.d("service $SERVICE_NAME server handler", e.toString())
                    }
                    Log.d("service $SERVICE_NAME server handler", "message added")
                }
                MessageType.MESSAGE_DELETE -> {
                    try{
                        CoroutineScope(Dispatchers.IO).launch {
                            messageRepository.deleteMessagesByMessageId(data.content)
                            Log.d("service $SERVICE_NAME server handler", "message: ${data.content} deleted")
                        }
                    }catch (e:Exception){
                        Log.d("service $SERVICE_NAME server handler", "exception while deleting message: $e")
                    }
                }

                MessageType.USER_CONNECT -> {
                    if (remoteIpAddress is InetSocketAddress) {
                        val user = gson.fromJson(data.content, User::class.java)
                            .copy(
                                ipAddr = remoteIpAddress.address.hostAddress!!,
                                port = data.ownServerPort
                            )
                        Log.d(
                            "service $SERVICE_NAME server handler",
                            "user unique: ${clientPartP2P.userOwner.value?.uniqueID}"
                        )
                        if (user.uniqueID != clientPartP2P.userOwner.value!!.uniqueID) {
                            Log.d(
                                "service $SERVICE_NAME server handler",
                                "received message-user from: $remoteIpAddress"
                            )
                            Log.d("service $SERVICE_NAME server handler", "received: $user")
                            clientPartP2P.addActiveUser(user)
                        } else {
                            Log.d(
                                "service $SERVICE_NAME server handler",
                                "massage from the same service"
                            )
                        }

                    } else {
                        throw Exception("Sender ip address is unknown")
                    }
                }
                MessageType.USER_UPDATE -> {
                    //todo обработка ошибок
                    val user = gson.fromJson(data.content, User::class.java)
                    userRepository.updateUser(user)
                }
                MessageType.USER_INFO_UPDATE -> {
                    //todo обработка ошибок
                    val userInfo = gson.fromJson(data.content, UserInfo::class.java)
                    Log.d("service $SERVICE_NAME server handler", "sender: ${data.senderId}")
                    val oldUserInfo = userRepository.getUserInfoById(data.senderId).first()
                    userRepository.updateUserInfo(
                        oldUserInfo.copy(info = userInfo.info,
                            about = userInfo.about)
                    )
                }
                MessageType.USER_FRIEND_DELETE ->{
                    val userToDelete = gson.fromJson(data.sender, User::class.java)
                    userRepository.deleteUser(userToDelete)
                    clientPartP2P.deleteFriendToStranger(userToDelete.uniqueID)
                }

                MessageType.STATUS -> {
                    TODO("Not implemented")
                }

                MessageType.HANDSHAKE -> {
                    TODO("Not implemented")
                }

                MessageType.REQUEST -> {
                    val request = gson.fromJson(data.content, Request::class.java)
                    val sender = gson.fromJson(data.sender, User::class.java)
                    when (request!!) {
                        Request.ADD -> {
                            /*** add a requested stranger*/
                            clientPartP2P.addPendingStranger(sender)
                            //friendResponseUseCase.addedFriendResponse(sender)
                        }//логика подтверждения\отклонения ведётся с клиента
                        Request.CANCEL -> {
                            /*** cancelling a requested stranger*/
                            clientPartP2P.removePendingStranger(sender)
                            //friendResponseUseCase.canceledFriendResponse(sender)
                        }

                        Request.REJECT -> {
                            /*** rejecting a requested stranger*/
                            Log.d("service $SERVICE_NAME server handler", "rejecting a requested stranger")
                            clientPartP2P.removeRequestedStranger(sender)
                            //friendResponseUseCase.rejectedFriendResponse(sender)
                        }

                        Request.DELETE -> {
                            /*** delete a friend*/
                            userRepository.deleteUser(sender)
                            //friendResponseUseCase.deletedFriendResponse(sender)
                        }

                        Request.APPROVE -> { //+зеркало
                            /*** approving a requested stranger*/
                            userRepository.insertUser(sender)
                            Log.d("service $SERVICE_NAME server handler", "approving a requested stranger ${sender.uniqueID}")
                            userRepository.insertUserInfo(UserInfo(sender.uniqueID,
                                iconImgURI = null,
                                bannerImgURI = null))

                            clientPartP2P.removeRequestedStranger(sender)

                            clientPartP2P.addStrangerToFriend(sender.uniqueID)

                            //friendResponseUseCase.approvedFriendResponse(sender)
                        }
                    }
                }
                MessageType.REQUEST_MEDIA_SERVER->{
                    /*** at mediadispatcher */ throw Exception("Skipped mediadispatcher")
                }
                else -> { TODO("not implemented yet OR skipped target handler") }
            }
        }
    }
}