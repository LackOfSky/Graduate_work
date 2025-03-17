package com.lackofsky.cloud_s.service.client.usecase

import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.StrangerRequestInterface
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Request
import com.lackofsky.cloud_s.service.model.TransportData
import javax.inject.Inject

class StrangerRequestUseCase @Inject constructor(val gson: Gson, val clientPartP2P: ClientPartP2P) :
    StrangerRequestInterface {
    override fun sendFriendRequest(sendTo: User):Boolean {
        return defaultStrangerRequest(sendTo,Request.ADD)
    }

    override fun approveFriendRequest(sendTo: User): Boolean {
        return defaultStrangerRequest(sendTo,Request.APPROVE)
    }

    override fun cancelFriendRequest(sendTo: User):Boolean {
        return defaultStrangerRequest(sendTo,Request.CANCEL)
    }

    override fun rejectFriendRequest(sendTo: User):Boolean {
        return defaultStrangerRequest(sendTo,Request.REJECT)
    }

//    override fun deleteFriendRequest(sendTo: User):Boolean {//Создать FriendRequestUseCase, и вынести туда
//        return defaultFriendRequest(sendTo,Request.DELETE)
//    }
    private fun defaultStrangerRequest(sendTo: User, request: Request):Boolean{
        val targetUser = clientPartP2P.activeStrangers.value.keys.find { it.uniqueID == sendTo.uniqueID }
        val client = clientPartP2P.activeStrangers.value.get(targetUser)
        if(client !=null){
            val content = gson.toJson(request)
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.REQUEST,
                senderId = clientPartP2P.userOwner.value!!.uniqueID,
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