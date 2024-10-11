package com.lackofsky.cloud_s.serviceP2P.client

import SecurityHandler
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
/***README
 * На каждого пира вверхней иерархии будет создаваться отдельный клиент, с которым будет вестись общение на уровне выше
 * как к ним стучаться - вопрос следующий..
 * */

class NettyClient(private val host: String, private val port: Int) {
    private lateinit var channel: Channel
    private val group = NioEventLoopGroup()

    fun connect() {
        try {
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        // Добавляем обработчики
                        //todo почитать об этом. у нас не используется protocol handler
                        pipeline.addLast(SecurityHandler(isClient = true))  // Обработчик Noise для шифрования
                        /*** abandoned*///pipeline.addLast(MplexHandler())
                        pipeline.addLast(NettyClientHandler())  // Основной обработчик сообщений
                    }
                })

            val channelFuture = bootstrap.connect(host, port).sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }
    fun sendMessage(message: String) {
        if (this::channel.isInitialized && channel.isActive) {
            channel.writeAndFlush(message)
        } else {
            println("Channel is not active")
            throw Exception()//todo отобразить ошибку в интерфейс
        }
    }

    fun close() {
        group.shutdownGracefully()
    }
}

