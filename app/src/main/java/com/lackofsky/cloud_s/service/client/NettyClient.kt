package com.lackofsky.cloud_s.service.client

import SecurityHandler
import android.util.Log
import com.google.gson.Gson
import com.lackofsky.cloud_s.service.data.SharedState
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import java.nio.charset.Charset

/***README
 * На каждого пира вверхней иерархии будет создаваться отдельный клиент, с которым будет вестись общение на уровне выше
 * как к ним стучаться - вопрос следующий..
 * */

class NettyClient(private val sharedState: SharedState, private val host: String, private val port: Int) {//
    private lateinit var channel: Channel
    private val group = NioEventLoopGroup()
    fun connect() {
        try {
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<SocketChannel>() {
//                    override fun channelActive(ctx: ChannelHandlerContext?) {
//                        super.channelActive(ctx)
//                        val content = gson.toJson(sharedState.userOwner.value)
//                        val transportData = TransportData(messageType = MessageType.USER,
//                            senderId = sharedState.userOwner.value!!.uniqueID,
//                            senderIp="",
//                            content =content )
//                        val json = gson.toJson(transportData)
//                        ctx!!.channel().writeAndFlush(json)//json
//                    }
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
//                        pipeline.addLast(LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
//                        pipeline.addLast(LengthFieldPrepender(4))
                        pipeline.addLast(StringDecoder(Charset.forName("UTF-8")))
                        pipeline.addLast(StringEncoder(Charset.forName("UTF-8")))
                        // Добавляем обработчики
                        //todo почитать об этом. у нас не используется protocol handler
                        //pipeline.addLast(SecurityHandler(isClient = true))  // Обработчик Noise для шифрования todo
                        /*** abandoned*///pipeline.addLast(MplexHandler())
                        pipeline.addLast(NettyClientHandler(sharedState))  // Основной обработчик сообщений
                    }

                    override fun channelInactive(ctx: ChannelHandlerContext?) {
                        super.channelInactive(ctx)
                        Log.d("service GrimBerry :client","client connection closed")
                    }
                })

            this.channel = bootstrap.connect(host, port).sync().channel()
            channel.closeFuture().sync()
        } catch (e: InterruptedException) {
            Log.e("NettyClient", "Connection error: ${e.message}")
        } finally {
            group.shutdownGracefully()
        }
    }
    fun sendMessage(message: String) {
        if (this::channel.isInitialized && channel.isActive) {
            channel.writeAndFlush(message)
//            Log.d("service GrimBerry :client","sended message: $message")
        } else {
            Log.e("service GrimBerry :client "," Channel is not active")
            throw Exception()//todo отобразить ошибку в интерфейс
        }
    }

    fun close() {
        group.shutdownGracefully()
    }
}

