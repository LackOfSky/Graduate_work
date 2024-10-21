package com.lackofsky.cloud_s.service.server.handlers

import android.util.Log
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import java.nio.charset.Charset

class LoggingHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        // Преобразуем сообщение в строку для логирования
        val message = msg.toString()
        // Логирование входящего сообщения
        if (msg is ByteBuf) {
            val message = msg.toString(Charsets.UTF_8)
            Log.i("service GrimBerry", "Received message from " + ctx.channel().remoteAddress() + message)// : $message
        } else {
            Log.e("service GrimBerry","Invalid message from " + ctx.channel().remoteAddress())//"Получено сообщение неизвестного типа: ${msg::class.java}"
        }
        ctx.fireChannelRead(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        // Логирование ошибки
        Log.e("service GrimBerry"+" LoggingHandler", "Channel error: ${cause.message}", cause)
        ctx.close() // Закрываем канал при ошибке
    }
}