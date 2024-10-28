package com.lackofsky.cloud_s.service.server

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.MessageType
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
    private val clientPartP2P: ClientPartP2P
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
                    if(remoteIpAddress is InetSocketAddress){
                        Log.d("service $SERVICE_NAME server handler","addr: "+ remoteIpAddress.address +remoteIpAddress.port)
                          //TODO
                        val user = gson.fromJson(data.content, User::class.java)
                            .copy(ipAddr = remoteIpAddress.address.hostAddress!!, port = remoteIpAddress.port)
//                        user.ipAddr =
//                        user.port = remoteIpAddress.port

                        Log.d("service $SERVICE_NAME server handler","received message-user from: $remoteIpAddress")
                        Log.d("service $SERVICE_NAME server handler","received: $user")
                    clientPartP2P.addActiveUser(user)
                    }else{
                        throw Exception("Sender ip address is unknown")
                    }
                }
                MessageType.USER_INFO -> {
                    TODO("переделать модель БД, реализовать прием данных")
                    val userInfo = gson.fromJson(data.content, UserInfo::class.java)
                    //sharedState.addFriendContent(userInfo)
                }
                MessageType.STATUS ->{
                    TODO("Not implemented")
                }
                MessageType.HANDSHAKE->{
                    TODO("Not implemented")
                }
            }
        }
    }
}