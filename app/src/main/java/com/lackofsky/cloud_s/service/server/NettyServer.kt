package com.lackofsky.cloud_s.service.server

import SecurityHandler
import android.util.Log
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.data.SharedState
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.server.handlers.LoggingHandler
import dagger.hilt.android.AndroidEntryPoint
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelProgressiveFutureListener
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.util.CharsetUtil
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import javax.inject.Inject

class NettyServer @Inject constructor(
    private val sharedState: SharedState,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
)  {
    private var port: Int = 15015
    private val serviceName = "GrimBerry"

    private lateinit var bossGroup: NioEventLoopGroup
    private lateinit var workerGroup: NioEventLoopGroup

    fun start() {

        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()
        Log.d("service $serviceName", "started at " + InetAddress.getLocalHost() +" $port")
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        Log.i("service $serviceName", "connected to " +
                                ch.pipeline().channel().remoteAddress() +" $port")
                        //pipeline.addLast(SecurityHandler(isClient = true))  // TODO
                        /*** abandoned*///pipeline.addLast(MplexHandler())  // Mplex для мультиплексирования
                        pipeline.addFirst(LoggingHandler())
                        pipeline.addLast(StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast(NettyServerHandler( // Основной обработчик сообщений
                            messageRepository,
                            userRepository,
                            sharedState)
                        )
                        ch.closeFuture().addListener { future ->//TODO (обработку ошибок)
                            sharedState.removeActiveUser(Peer(name = "",
                                                        address = ch.remoteAddress().address.hostAddress!!,
                                                        port = ch.remoteAddress().port) )
                            Log.i("service $serviceName", "Connection closed: ${ch.remoteAddress()}")

                        }
                    }
                })

            val channelFuture = bootstrap.bind(setPort()).sync()
            Log.d("service $serviceName", "started at " + InetAddress.getLocalHost() +" $port")
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            Log.d("service $serviceName", "stopped")
        }
    }

    fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        Log.d("service $serviceName", "stopped")
    }
    private fun setPort():Int{
        while (isPortInUse(port)) {
            port++
        }
        return port
    }
    private fun isPortInUse(port: Int): Boolean {
        return try {
            ServerSocket(port).use { false }
        } catch (e: IOException) {
            true
        }
    }
}