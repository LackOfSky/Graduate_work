package com.lackofsky.cloud_s.service.client

import android.util.Log
import com.google.gson.GsonBuilder
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.model.ResponseData
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaClient
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import com.lackofsky.cloud_s.service.server.MediaResponse
import com.lackofsky.cloud_s.service.server.MediaResponseStatus
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NettyClientHandler : SimpleChannelInboundHandler<String>() {//val sharedState:SharedState
    val gson = GsonBuilder().create()
    override fun channelActive(ctx: ChannelHandlerContext?) {
    }
    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        // Обрабатываем входящие сообщения
        println("Received response: $msg")

        ctx.fireChannelRead(msg)
        val data = gson.fromJson(msg, ResponseData::class.java)
        Log.d("service $SERVICE_NAME server handler", "received: $data")
        //processMessage(ctx, data)
    }
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        Log.e("service GrimBerry :client", "Error: ${cause.message}")
        ctx.close()
    }

//    private fun processMessage(ctx: ChannelHandlerContext, data: ResponseData) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val mediaResponse = gson.fromJson(data.responseContent, MediaResponse::class.java)
//            when (mediaResponse.status) {
//                MediaResponseStatus.QUEUETED -> TODO()
//                MediaResponseStatus.ACCEPTED -> {
//                    when(mediaResponse.requestedIntend){
//                        TransferMediaIntend.MEDIA_USER_LOGO -> {
//                            //TODO(открыть клиент)
////                            nettyMediaClient.sendUserLogoFile(
////
////                            )
//                            mediaResponse.msIpAddress
//                            mediaResponse.msPort
//                        }
//                        TransferMediaIntend.MEDIA_USER_BANNER -> {
//                            //TODO(открыть клиент)
//                            mediaResponse.msIpAddress
//                            mediaResponse.msPort
//
//                        }
//                        TransferMediaIntend.MEDIA_EXTERNAL -> TODO()
//                        null -> TODO()
//                    }
//                }
//                MediaResponseStatus.REJECTED -> TODO()
//            }
//        }
//    }
}