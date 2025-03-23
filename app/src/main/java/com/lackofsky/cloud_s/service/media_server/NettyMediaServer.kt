package com.lackofsky.cloud_s.service.media_server

import android.content.Context
import android.util.Log
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.media_server.handlers.MediaHandler
import javax.inject.Inject
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.server.MediaDispatcher
import com.lackofsky.cloud_s.service.server.handlers.LoggingHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.handler.traffic.GlobalTrafficShapingHandler
import java.net.InetSocketAddress

class NettyMediaServer @Inject constructor(
    private val context: Context,
    private val metadata: Metadata,
    private val mediaDispatcher: MediaDispatcher,
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) {
    private val serviceName = metadata.serviceName
    private var boundPort = 0
    private lateinit var bossGroup: NioEventLoopGroup
    private lateinit var workerGroup: NioEventLoopGroup

    fun start() {
        Log.d("service $serviceName. nettyMediaServer", "starting nettyMediaServer")

        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        mediaDispatcher.setMediaServerAddress(
                            ch.localAddress().hostName,
                            boundPort )
                        Log.d("service $serviceName media server", "initChannel")

                        val pipeline = ch.pipeline()
                        Log.i("service $serviceName media server", "Netty media server. Connected as server to " + ch.pipeline().channel().remoteAddress())
                        //pipeline.addLast(GlobalTrafficShapingHandler(workerGroup, 5_000_000, 5_000_000))
                        //pipeline.addLast(ChunkedWriteHandler()) // Потокова передача
                        pipeline.addLast(MediaHandler(context,
                            userRepository = userRepository,
                            messageRepository = messageRepository)) // Обробник медіафайлів
                    }
                })

            val channelFuture = bootstrap.bind(boundPort).sync()
            boundPort = (channelFuture.channel().localAddress() as InetSocketAddress).port

            Log.d("service $serviceName", "media server started at port: $boundPort")
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            Log.d("$serviceName netty media server", "stopped")
        }
    }

    fun getPort(): Int {
        return boundPort
    }

    fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}