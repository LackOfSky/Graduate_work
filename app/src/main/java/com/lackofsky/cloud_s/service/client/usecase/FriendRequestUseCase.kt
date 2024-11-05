package com.lackofsky.cloud_s.service.client.usecase

import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.RequestInterface
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Request
import com.lackofsky.cloud_s.service.model.TransportData
import javax.inject.Inject

class FriendRequestUseCase @Inject constructor( val gson: Gson, val clientPartP2P: ClientPartP2P) :RequestInterface {
    override fun sendFriendRequest(user: User):Boolean {
        return defaultFriendRequest(user,Request.ADD)
    }

    override fun cancelFriendRequest(user: User):Boolean {
        return defaultFriendRequest(user,Request.CANCEL)
    }

    override fun rejectFriendRequest(user: User):Boolean {
        return defaultFriendRequest(user,Request.REJECT)
    }

    override fun deleteFriendRequest(user: User):Boolean {
        return defaultFriendRequest(user,Request.DELETE)
    }
    private fun defaultFriendRequest(user: User, request: Request):Boolean{
        val client = clientPartP2P.activeFriends.value.get(user)
        if(client !=null){
            val content = gson.toJson(request)
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.REQUEST,
                senderId = clientPartP2P.userOwner.value!!.uniqueID,
                senderIp = "",
                sender = sender,
                content = content
            )
            val json = gson.toJson(transportData)
            client.sendMessage(json)
        }else{
            //throw Exception("BerryGrim. Attempt to send message to non-active channel.. P2PClient")
            return false
        }
        return true
    }
}