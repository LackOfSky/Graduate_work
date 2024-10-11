package com.lackofsky.cloud_s.serviceP2P.netty

import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.serviceP2P.model.MessageType
import com.lackofsky.cloud_s.serviceP2P.model.TransportData
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import javax.inject.Inject

class NettyServerHandler@Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : SimpleChannelInboundHandler<String>() {
    val gson = Gson()
    // Десериализация JSON в объект TransportData


    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {

            processMessage(
                gson.fromJson(msg, TransportData::class.java)
            )

        // Обрабатываем входящие сообщения
//        println("Received message: $msg")
        ctx.channel().remoteAddress()
        ctx.writeAndFlush("Message received")//TODO обработка логики подтверждения приема сообщения
//        ctx.name()
//        ctx.channel().id()
    }
    private fun processMessage(data: TransportData) {
        // Здесь можно обработать сообщение и взаимодействовать с messageRepository
        CoroutineScope(Dispatchers.IO).launch {
            when (data.messageType) {
                MessageType.MESSAGE -> {
                    messageRepository.insertMessage(
                        gson.fromJson(data.content, Message::class.java)
                    )
                }
                MessageType.USER -> {
                    userRepository.insertUser(
                        gson.fromJson(data.content, User::class.java)
                    )
                }
                MessageType.USER_INFO -> {
                    userRepository.insertUserInfo(
                        gson.fromJson(data.content, UserInfo::class.java)
                    )
                }

            }
        }
    }
}