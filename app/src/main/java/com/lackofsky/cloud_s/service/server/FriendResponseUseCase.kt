package com.lackofsky.cloud_s.service.server

import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Response
import com.lackofsky.cloud_s.service.model.TransportData
import javax.inject.Inject

class FriendResponseUseCase @Inject constructor(private val gson: Gson,
                                                private val clientPartP2P: ClientPartP2P):ResponseInterface {
    override fun approveFriendResponse(user: User): Boolean {
        return defaultFriendResponse(user,Response.APPROVED)
    }

    override fun cancelFriendResponse(user: User): Boolean {
        return defaultFriendResponse(user,Response.CANCELED)
    }

    override fun rejectFriendResponse(user: User): Boolean {
        return defaultFriendResponse(user,Response.REJECTED)
    }

    override fun deleteFriendResponse(user: User): Boolean {
        return defaultFriendResponse(user,Response.DELETED)
    }
    private fun defaultFriendResponse(user: User,response: Response): Boolean{
        val client = clientPartP2P.activeFriends.value.get(user)
        if(client !=null){
            val content = gson.toJson(response)
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.RESPONSE,
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