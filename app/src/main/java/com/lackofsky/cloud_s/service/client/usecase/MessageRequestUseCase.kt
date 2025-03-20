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

    override fun deleteMessageOne2OneRequest(sendTo: NettyClient, messageKey: MessageKey): Boolean {
        val contentJSON = gson.toJson(messageKey)
        return defaultMessageRequest(sendTo = sendTo, contentJSON = contentJSON, messageType = MessageType.MESSAGE_DELETE)
    }
    private fun defaultMessageRequest(sendTo: NettyClient, contentJSON: String, messageType: MessageType): Boolean {
        try {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = messageType,
                senderId = clientPartP2P.userOwner.value!!.uniqueID,
                sender = sender,
                content = contentJSON
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
}