package com.lackofsky.cloud_s.service.client.usecase

import android.util.Log
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.FriendRequestInterface
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.client.StrangerRequestInterface
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import javax.inject.Inject

class FriendRequestUseCase @Inject constructor(val gson: Gson, val clientPartP2P: ClientPartP2P) :
    FriendRequestInterface {
    override fun deleteFriendRequest(sendTo: NettyClient): Boolean {
        return defaultFriendRequest(sendTo, messageType = MessageType.USER_FRIEND_DELETE)
    }

    private fun defaultFriendRequest(client: NettyClient, content: String = "", messageType: MessageType):Boolean{
        try {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = messageType,
                senderId = clientPartP2P.userOwner.value!!.uniqueID,
                sender = sender,
                content = content
            )
            val json = gson.toJson(transportData)
            client.sendMessage(json)
        }catch (e: Exception){
            Log.d("service $SERVICE_NAME :friendRequestUseCase", "defaultFriendRequest: exception $e")
            return false
        }


        return true
    }
}