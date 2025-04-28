package com.lackofsky.cloud_s.service.client.usecase

import android.util.Log
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.MessageRequestInterface
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageKey
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import com.lackofsky.cloud_s.service.server.MediaRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageRequestUseCase @Inject constructor(
    val gson: Gson, val clientPartP2P: ClientPartP2P
) : MessageRequestInterface {
    /***
     * Ключ унікальності знаходження потрібного повідомлення = equals: chatName + date + content hashCode
     * Клас MessageKey
     * */

//    override fun sendMessageRequest(
//        activeFriend: List<User>, content: Message
//    ): Boolean {
//        TODO()
////        val contentJSON = gson.toJson(content)
////        activeFriend.forEach { to ->
////            defaultMessageRequest(to, contentJSON, MessageType.MESSAGE)
////        }
//        return true
//    }
    override fun sendMessageRequest(
        sendTo: NettyClient, message: Message
    ): Boolean {
        val contentJSON = gson.toJson(message)
        return defaultMessageRequest(sendTo, contentJSON, MessageType.MESSAGE)
    }
    fun sendMediaMessageRequest(sendTo: NettyClient, message: Message):Boolean{
                    Log.e("GrimBerry ChangesNotifierUseCase", "userInfoMediaChangesNotifierRequest: TransferMediaIntend.MEDIA_EXTERNAL is not allowed here")
                    val user = clientPartP2P.userOwner.value
                    val content = gson.toJson(
                        MediaRequest(TransferMediaIntend.MEDIA_EXTERNAL, userUniqueId = user!!.uniqueID, message.uniqueId)
                    )

                    return mediaNotifierRequest(sendTo, content, MessageType.REQUEST_MEDIA_SERVER)


                //return mediaNotifierRequest(sendTo, content, MessageType.REQUEST_MEDIA_SERVER)

        }


    override fun deleteMessageOne2OneRequest(sendTo: NettyClient, messageUniqueId: String): Boolean {
        return defaultMessageRequest(sendTo = sendTo, content = messageUniqueId, messageType = MessageType.MESSAGE_DELETE)
    }
    private fun defaultMessageRequest(sendTo: NettyClient, content: String, messageType: MessageType): Boolean {
        try {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = messageType,
                senderId = clientPartP2P.userOwner.value!!.uniqueID,
                sender = sender,
                content = content
            )
            Log.d("service $SERVICE_NAME :messageRequestUseCase", "defaultMessageRequest: $transportData")
            val json = gson.toJson(transportData)
            sendTo.sendMessage(json)
        }catch (e: Exception){
            Log.d("service $SERVICE_NAME :messageRequestUseCase", "defaultMessageRequest: exception $e")
            return false
        }
        return true
    }

    private fun mediaNotifierRequest(sendTo: NettyClient, content: String, messageType: MessageType): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
//            try{
//                sendTo.forEach { client ->
                    try{
                        val transportData = TransportData(
                            messageType = messageType,
                            senderId = clientPartP2P.userOwner.value!!.uniqueID,
                            sender = sender,
                            content = content
                        )
                        val json = gson.toJson(transportData)
                        sendTo.sendMessage(json)
                    }catch (e: Exception){
                        Log.d("service $SERVICE_NAME :changesNotifierUseCase", "mediaNotifierRequest: exception $e at client ip ${sendTo.getChannelIpAddress()}")
                        throw e
//                    }
                }
        }
        return true //TODO
    }
}