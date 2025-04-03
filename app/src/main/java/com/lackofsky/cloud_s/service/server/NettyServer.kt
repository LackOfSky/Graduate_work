package com.lackofsky.cloud_s.service.server

import android.util.Log
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaClient
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaServer
import com.lackofsky.cloud_s.service.server.handlers.LoggingHandler
import com.lackofsky.cloud_s.service.server.handlers.MediaHandler
import com.lackofsky.cloud_s.service.server.handlers.MessageHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.charset.Charset
import javax.inject.Inject
import java.net.NetworkInterface

class NettyServer @Inject constructor(
    private val clientPartP2P: ClientPartP2P,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val metadata: Metadata,
    private val mediaDispatcher: MediaDispatcher,
    private val mediaServer: NettyMediaServer,
    private val mediaClient: NettyMediaClient
)  {
    private val serviceName = metadata.serviceName
    private var boundPort = 0
    private lateinit var bossGroup: NioEventLoopGroup
    private lateinit var workerGroup: NioEventLoopGroup

    /*** returns service port value*/
     fun start() {
        Log.d("service $serviceName. nettyServer", "starting nettyServer")

        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        Log.i("service $serviceName server" , "Netty server. Connected as server to " +
                                ch.pipeline().channel().remoteAddress())
                        //pipeline.addLast(SecurityHandler(isClient = true))  // TODO
                        //pipeline.addLast(LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                        //pipeline.addLast(LengthFieldPrepender(4))
                        pipeline.addLast(StringDecoder(Charset.forName("UTF-8")))
                        pipeline.addLast(StringEncoder(Charset.forName("UTF-8")))

                        pipeline.addFirst(LoggingHandler())
//                        pipeline.addLast(MessageHandler( // handler для повідомлень]
//                            messageRepository,
//                            userRepository,
//                            chatRepository,
//                            clientPartP2P))
                        pipeline.addLast(MediaHandler(userRepository, mediaDispatcher,
                            mediaClient, clientPartP2P, mediaServer))
                        pipeline.addLast(NettyServerHandler( // Основний обробник для всіх повідомлень (на данному етапі)
                            messageRepository,
                            userRepository,
                            chatRepository,
                            clientPartP2P)
                        )
                    }
                })

            val channelFuture = bootstrap.bind(boundPort).sync()
                boundPort = (channelFuture.channel().localAddress() as InetSocketAddress).port

                Log.d("service $serviceName", "started at port: $boundPort")//InetAddress.getLocalHost()
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            Log.d("$serviceName netty server", "stopped")

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