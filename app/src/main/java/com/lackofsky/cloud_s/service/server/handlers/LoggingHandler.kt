package com.lackofsky.cloud_s.service.server.handlers

import android.util.Log
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.nio.charset.Charset

class LoggingHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        // Преобразуем сообщение в строку для логирования
        // Логирование входящего сообщения
        if (msg is ByteBuf) {
            val message = msg.toString(Charsets.UTF_8)
            Log.i("service GrimBerry LoggHandler", "Received message from " + ctx.channel().remoteAddress() + message)// : $message
            ctx.fireChannelRead(msg)//Send message to next pipeline
        } else {
            Log.e("service GrimBerry LoggHandler","Invalid message from " + ctx.channel().remoteAddress())//"Получено сообщение неизвестного типа: ${msg::class.java}"
        }

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Логирование ошибки
        Log.e("service GrimBerry"+" LoggingHandler", "Channel error: ${cause.message}", cause)
        ctx.close() // Закрываем канал при ошибке
    }
}