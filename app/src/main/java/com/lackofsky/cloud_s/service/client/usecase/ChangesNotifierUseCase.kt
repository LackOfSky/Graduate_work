package com.lackofsky.cloud_s.service.client.usecase

import android.util.Log
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.ChangesNotifierRequestInterface
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChangesNotifierUseCase @Inject constructor(val gson: Gson, val clientPartP2P: ClientPartP2P) :
    ChangesNotifierRequestInterface{

    override fun userChangesNotifierRequest(sendTo: List<NettyClient>, user: User): Boolean {
        val content = gson.toJson(user)
        return defaultNotifierRequest(sendTo, content, MessageType.USER_UPDATE)
    }
    override fun userInfoChangesNotifierRequest(sendTo: List<NettyClient>, userInfo: UserInfo): Boolean {
        val info = userInfo.copy(bannerImgURI = "", iconImgURI = "")
        val content = gson.toJson(info)
        //TODO SEND MEDIA REQUEST
        return defaultNotifierRequest(sendTo, content, MessageType.USER_UPDATE)
    }


    /***NOTIFICATION ABOUT ACCOUNT CHANGES TO ALL ACTIVE CONNECTION
     * sendTo = all active connections
     * content = json ( User, UserInfo, etc. ..
     * */
    private fun defaultNotifierRequest(sendTo: List<NettyClient>, content: String, messageType: MessageType): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            sendTo.forEach { client ->
                try{
                    val transportData = TransportData(
                        messageType = messageType,
                        senderId = clientPartP2P.userOwner.value!!.uniqueID,
                        sender = sender,
                        content = content
                    )
                    val json = gson.toJson(transportData)
                    client.sendMessage(json)
                }catch (e: Exception){
                    Log.d("service $SERVICE_NAME :changesNotifierUseCase", "defaultNotifierRequest: exception $e")
                }
            }
        }
            return true
    }

}