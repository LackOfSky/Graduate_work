package com.lackofsky.cloud_s.serviceP2P.netty

import SecurityHandler
import com.lackofsky.cloud_s.serviceP2P.model.Peer
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import javax.inject.Inject

class NettyServer(private val port: Int, private val peers:MutableSet<Peer>) {
    @Inject
    private lateinit var nettyServerHandler: NettyServerHandler
    private lateinit var bossGroup: NioEventLoopGroup
    private lateinit var workerGroup: NioEventLoopGroup

    private val peerMap = HashMap<Int, Peer>()

    fun start() {
        bossGroup = NioEventLoopGroup(1)
        workerGroup = NioEventLoopGroup()

        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
                        toMap(ch)
                        // Добавляем обработчики
                        ch.localAddress()
                        pipeline.addLast(SecurityHandler(isClient = true))  // Обработчик Noise для шифрования
                        /*** abandoned*///pipeline.addLast(MplexHandler())  // Mplex для мультиплексирования
                        pipeline.addLast(nettyServerHandler)  // Основной обработчик сообщений
                    }
                })

            val channelFuture = bootstrap.bind(port).sync()
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
    private fun toMap(ch: SocketChannel){
        if(peerMap.contains(ch.id().toString().toInt())) return

        peers.forEach { peer ->
            if(peer.address == ch.localAddress().address.hostAddress){
                peerMap.put(ch.id().toString().toInt(), peer)
            }else{
                throw Exception() //TODO
            }
        }
    }
}