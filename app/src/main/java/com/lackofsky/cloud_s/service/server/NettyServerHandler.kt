package com.lackofsky.cloud_s.service.server

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.usecase.FriendRequestUseCase
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Request
import com.lackofsky.cloud_s.service.model.Response
import com.lackofsky.cloud_s.service.model.TransportData
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class NettyServerHandler(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val clientPartP2P: ClientPartP2P,
    //private val friendResponseUseCase: FriendResponseUseCase
) : SimpleChannelInboundHandler<String>() {
    val gson = Gson()

    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        try {
                val data = gson.fromJson(msg, TransportData::class.java)
            Log.d("service $SERVICE_NAME server handler","received: $data")
                processMessage(ctx, data)
        }catch (e: JsonParseException) {
            Log.e("service GrimBerry SH"+" NettyServerHandler", "Error parsing message: ")
            Log.d("service GrimBerry SH", "non typical message: "+msg)
        }catch (e: IllegalStateException){
            Log.e("service GrimBerry SH"+" NettyServerHandler", "Error json syntax message: $msg")
            Log.d("service GrimBerry SH", "non typical message: "+msg)
        } catch (_: Exception){
            Log.e("service GrimBerry SH", "received unknown type of message:" +msg)
        }
        ctx.writeAndFlush("Message received from "+ctx.channel().remoteAddress())//TODO обработка логики подтверждения приема сообщенияс

    }
    private fun processMessage(ctx: ChannelHandlerContext,data: TransportData) {
        // Здесь можно обработать сообщение и взаимодействовать с messageRepository
        CoroutineScope(Dispatchers.IO).launch {
            val remoteIpAddress = ctx.pipeline().channel().remoteAddress()

            when (data.messageType) {
                MessageType.MESSAGE -> {
                    messageRepository.insertMessage(
                        gson.fromJson(data.content, Message::class.java)
                    )
                }

                MessageType.USER -> {
                    if (remoteIpAddress is InetSocketAddress) {
                        Log.d(
                            "service $SERVICE_NAME server handler",
                            "addr: " + remoteIpAddress.address + remoteIpAddress.port
                        )
                        //TODO
                        val user = gson.fromJson(data.content, User::class.java)
                            .copy(
                                ipAddr = remoteIpAddress.address.hostAddress!!,
                                port = remoteIpAddress.port
                            )
//                        user.ipAddr =
//                        user.port = remoteIpAddress.port

                        Log.d(
                            "service $SERVICE_NAME server handler",
                            "received message-user from: $remoteIpAddress"
                        )
                        Log.d("service $SERVICE_NAME server handler", "received: $user")
                        clientPartP2P.addActiveUser(user)
                    } else {
                        throw Exception("Sender ip address is unknown")
                    }
                }

                MessageType.USER_INFO -> {
                    TODO("переделать модель БД, реализовать прием данных")
                    val userInfo = gson.fromJson(data.content, UserInfo::class.java)
                    //sharedState.addFriendContent(userInfo)
                }

                MessageType.STATUS -> {
                    TODO("Not implemented")
                }

                MessageType.HANDSHAKE -> {
                    TODO("Not implemented")
                }

//                MessageType.RESPONSE -> {
//                    val response = gson.fromJson(data.content, Response::class.java)
//                    val sender = gson.fromJson(data.sender, User::class.java)
//                    when (response!!) {
//                        Response.APPROVED -> {
//                            clientPartP2P.removePendingStranger(sender)
//                            userRepository.insertUser(sender)
//                        }
//                        Response.REJECTED -> {
//                            clientPartP2P.removePendingStranger(sender)
//                        }
//                        Response.CANCELED -> {
//                            clientPartP2P.removeRequestedStranger(sender)
//                        }
//                        Response.DELETED -> {
//                            userRepository.deleteUser(sender)// TODO(проверить логику добавления и удаления пользователей из базы данных)
//                        }
//                        Response.ADDED -> {
//                            clientPartP2P.addRequestedStranger(sender)
//                        }
//                    }
//
//                }

                MessageType.REQUEST -> {
                    val request = gson.fromJson(data.content, Request::class.java)
                    val sender = gson.fromJson(data.sender, User::class.java)
                    when (request!!) {
                        Request.ADD -> {/*** add a requested stranger*/
                            clientPartP2P.addPendingStranger(sender)
                            //friendResponseUseCase.addedFriendResponse(sender)
                        }//логика подтверждения\отклонения ведётся с клиента
                        Request.CANCEL -> {/*** cancelling a requested stranger*/
                            clientPartP2P.removePendingStranger(sender)
                            //friendResponseUseCase.canceledFriendResponse(sender)
                        }
                        Request.REJECT -> {/*** rejecting a requested stranger*/
                            clientPartP2P.removeRequestedStranger(sender)
                            //friendResponseUseCase.rejectedFriendResponse(sender)
                        }
                        Request.DELETE -> {/*** delete a friend*/
                            userRepository.deleteUser(sender)
                            //friendResponseUseCase.deletedFriendResponse(sender)
                        }
                        Request.APPROVE -> {/*** approving a requested stranger*/
                            userRepository.insertUser(sender)
                            clientPartP2P.removeRequestedStranger(sender)
                            //friendResponseUseCase.approvedFriendResponse(sender)
                        }
                    }
                }
            }
        }
    }
}