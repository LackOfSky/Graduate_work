package com.lackofsky.cloud_s.serviceP2P.client

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class NettyClientHandler : SimpleChannelInboundHandler<String>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        // Обрабатываем входящие сообщения
        println("Received response: $msg")

        ctx.fireChannelRead(msg)
    }
}