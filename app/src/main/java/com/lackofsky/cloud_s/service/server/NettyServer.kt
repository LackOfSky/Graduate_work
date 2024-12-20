package com.lackofsky.cloud_s.service.server

import android.util.Log
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.server.handlers.LoggingHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
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
//    private val friendResponseUseCase: FriendResponseUseCase
)  {
    private val serviceName = metadata.serviceName

    private lateinit var bossGroup: NioEventLoopGroup
    private lateinit var workerGroup: NioEventLoopGroup

    /*** returns service port value*/
    fun start():Int {
        var boundPort = 0
        //Log.d("service $serviceName"+ " ip-addr :",getLocalIpAddress()!!)
        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()

                        Log.i("service $serviceName server" , "connected to " +
                                ch.pipeline().channel().remoteAddress())
                        //pipeline.addLast(SecurityHandler(isClient = true))  // TODO
                        /*** abandoned*///pipeline.addLast(MplexHandler())  // Mplex для мультиплексирования
                        //pipeline.addLast(LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
                        //pipeline.addLast(LengthFieldPrepender(4))
                        pipeline.addLast(StringDecoder(Charset.forName("UTF-8")))
                        pipeline.addLast(StringEncoder(Charset.forName("UTF-8")))

                        pipeline.addFirst(LoggingHandler())
                        pipeline.addLast(NettyServerHandler( // Основной обработчик сообщений
                            messageRepository,
                            userRepository,
                            chatRepository,
                            clientPartP2P)
                        )
                        ch.closeFuture().addListener { future ->//TODO (обработку ошибок)
                            clientPartP2P.removeActiveUser(Peer(name = "",
                                                        address = ch.remoteAddress().address.address.toString())
                            )
                            Log.i("service $serviceName", "Connection closed: ${ch.remoteAddress()}")

                        }
                    }
                })

            val channelFuture = bootstrap.bind(boundPort).sync().also { channelFuture ->
                boundPort = (channelFuture.channel().localAddress() as InetSocketAddress).port
                Log.d("service $serviceName", "started at " + InetAddress.getLocalHost() +" $boundPort")
            }.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            Log.d("service $serviceName", "stopped")

        }
        return boundPort
    }

    fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        Log.d("service $serviceName", "stopped")
    }

//    fun getLocalIpAddress(): String? {
//
//        Log.d(
//            "service $serviceName" + " ip-addr :",
//            NetworkInterface.getNetworkInterfaces().toString()
//        )
//        for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
//            Log.d("service $serviceName"+ " ip-addr :",networkInterface.name.toString()+networkInterface.inetAddresses.toString()+networkInterface.interfaceAddresses.toString())
//            when {
//                networkInterface.name.startsWith("p2p") -> {
//                    /* Wi-Fi Direct */ }
//                networkInterface.name.startsWith("bnep") -> { /* Bluetooth PAN */ }
//                networkInterface.name.startsWith("rndis") -> { /* USB Tethering */ }
//                else -> { /* Other */ }
//            }
//        }
//
//        val interfaces = NetworkInterface.getNetworkInterfaces()
//        for (networkInterface in interfaces) {
//            val addresses = networkInterface.inetAddresses
//            for (address in addresses) {
//                if (!address.isLoopbackAddress && address is InetAddress) {
//                    val hostAddress = address.hostAddress
//                    // Проверяем, что это IPv4-адрес
//                    if (hostAddress.indexOf(':') < 0) {
//                        return hostAddress
//                    }
//                }
//            }
//        }
//        return null
//    }
}