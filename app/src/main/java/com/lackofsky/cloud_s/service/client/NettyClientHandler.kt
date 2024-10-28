package com.lackofsky.cloud_s.service.client

import android.util.Log
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class NettyClientHandler() : SimpleChannelInboundHandler<String>() {//val sharedState:SharedState
    override fun channelActive(ctx: ChannelHandlerContext?) {
//        val gson = Gson()
//        val content = gson.toJson(sharedState.userOwner.value)
//        val transportData = TransportData(messageType = MessageType.USER,
//            senderId = sharedState.userOwner.value!!.uniqueID,
//            senderIp="",
//            content =content )
//        val json = gson.toJson(transportData)
//        ctx!!.channel().writeAndFlush(json)//json
//
//        //ctx!!.channel().writeAndFlush("hello world")//json
//        Log.d("service GrimBerry :client","sended $json")//$json"
//        super.channelActive(ctx)
    }
    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        // Обрабатываем входящие сообщения
        println("Received response: $msg")

        ctx.fireChannelRead(msg)
    }
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Log.e("service GrimBerry :client", "Error: ${cause.message}")
        ctx.close()
    }
}