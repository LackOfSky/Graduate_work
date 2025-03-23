package com.lackofsky.cloud_s.service.client

import android.util.Log
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.string.StringDecoder
import io.netty.handler.codec.string.StringEncoder
import java.net.InetSocketAddress
import java.nio.charset.Charset

/***README
 * На каждого пира вверхней иерархии будет создаваться отдельный клиент, с которым будет вестись общение на уровне выше
 * как к ним стучаться - вопрос следующий..
 * */

class NettyClient( private val host: String, private val port: Int) {//private val sharedState: SharedState,
    private var channel: Channel? = null
    private var group: EventLoopGroup? = null
    val TAG = "GrimBerry NettyClient"
    fun connect(addActiveUserCallback: (()->Unit)? = null ,
                removeActiveUserCallback:(()->Unit)? = null) {
            group = NioEventLoopGroup()
            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        val pipeline = ch.pipeline()
//                        pipeline.addLast(LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4))
//                        pipeline.addLast(LengthFieldPrepender(4))
                        pipeline.addLast(StringDecoder(Charset.forName("UTF-8")))
                        pipeline.addLast(StringEncoder(Charset.forName("UTF-8")))
                        //pipeline.addLast(SecurityHandler(isClient = true))  // Обработчик  для шифрования todo
                        pipeline.addLast(NettyClientHandler())  // Основной обработчик сообщений sharedState
                        ch.closeFuture().addListener {
                            removeActiveUserCallback?.apply {
                                Log.d("service GrimBerry :client","removeActiveUserCallback invoked")
                                invoke()
                            }
                        }
                    }

                    override fun channelInactive(ctx: ChannelHandlerContext?) {
                        Log.d("service GrimBerry :client","client connection closed.Host: $host, Port: $port")
                        super.channelInactive(ctx)
                    }
                })

            val channelFuture = bootstrap.connect(host, port).sync()
            channel = channelFuture.channel()
            addActiveUserCallback?.invoke()
            Log.d(TAG,"Netty Client. Connected as client to $host:$port. SS61")
    }
    fun sendMessage(message: String) {
        //if (this::channel.isInitialized && channel!!.isActive) {
        if (channel?.isOpen == true) {
            channel?.let {
                val future = it.writeAndFlush(message)
                future.addListener {
                    if (it.isSuccess) {
                        Log.d("service GrimBerry :client", "sent message: $message")
                    } else {
                        Log.d("service GrimBerry :client", "Message sending failed", future.cause())
                    }
                }
                return
            }
        }else{
            Log.e("service GrimBerry :client ","Channel is closed ")
            throw Exception("service GrimBerry :client ,Channel is closed")
        }

    }
    fun getIpAddress(): String? {
        val socketAddress = channel?.remoteAddress() as InetSocketAddress
        return socketAddress.hostName
    }
    fun getPort(): Int {
        val socketAddress = channel?.remoteAddress() as InetSocketAddress
        return socketAddress.port
    }
    fun close() {
        channel?.close()?.sync()
        group?.shutdownGracefully()
        Log.d("service GrimBerry :client","client connection closed.Host: $host, Port: $port")
    }
}

