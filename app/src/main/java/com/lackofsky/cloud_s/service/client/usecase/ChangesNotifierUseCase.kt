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
import com.lackofsky.cloud_s.service.server.MediaRequest
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
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
    override fun userInfoTextChangesNotifierRequest(sendTo: List<NettyClient>, userInfo: UserInfo): Boolean {
        val info = userInfo.copy(bannerImgURI = "", iconImgURI = "")
        val content = gson.toJson(info)
        //TODO SEND MEDIA REQUEST
        return defaultNotifierRequest(sendTo, content, MessageType.USER_UPDATE)
    }

    /*** !ONLY TransferMediaIntend.MEDIA_USER_BANNER, TransferMediaIntend.MEDIA_USER_LOGO
     *
     * returns false if transferIntend = TransferMediaIntend.MEDIA_EXTERNAL
     * */
    override fun userInfoMediaChangesNotifierRequest(
        sendTo: List<NettyClient>,
        transferIntend: TransferMediaIntend
    ): Boolean {
        if (transferIntend == TransferMediaIntend.MEDIA_EXTERNAL){
            Log.e("GrimBerry ChangesNotifierUseCase", "userInfoMediaChangesNotifierRequest: TransferMediaIntend.MEDIA_EXTERNAL is not allowed here")
            return false
        }
        val user = clientPartP2P.userOwner.value
        val content = gson.toJson(
            MediaRequest(transferIntend, userUniqueId = user!!.uniqueID, null)
        )
        return mediaNotifierRequest(sendTo, content, MessageType.REQUEST_MEDIA_SERVER)
    }


    /***NOTIFICATION ABOUT ACCOUNT CHANGES TO ALL ACTIVE CONNECTION
     * sendTo = all active connections
     * content = json ( User, UserInfo, etc. ..
     * */
    private fun defaultNotifierRequest(sendTo: List<NettyClient>, content: String, messageType: MessageType): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            try{
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
                        Log.d("service $SERVICE_NAME :changesNotifierUseCase", "defaultNotifierRequest: exception $e at client ip ${client.getChannelIpAddress()}")
                    }
                }
            }catch (e: Exception){
                Log.d("service $SERVICE_NAME :changesNotifierUseCase", "defaultNotifierRequest: exception $e")
            }

        }
            return true
    }
    private fun mediaNotifierRequest(sendTo: List<NettyClient>, content: String, messageType: MessageType): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            try{
                sendTo.forEach { client ->
                    try{
                        val transportData = TransportData(
                            messageType = messageType,
                            senderId = clientPartP2P.userOwner.value!!.uniqueID,
                            sender =sender,
                            content = content
                        )
                        val json = gson.toJson(transportData)
                        client.sendMessage(json)
                    }catch (e: Exception){
                        Log.d("service $SERVICE_NAME :changesNotifierUseCase", "mediaNotifierRequest: exception $e at client ip ${client.getChannelIpAddress()}")
                        throw e
                    }
                }
            }catch (e: Exception){
                Log.d("service $SERVICE_NAME :changesNotifierUseCase", "mediaNotifierRequest: exception $e")
            }

            }
            return true //TODO
        }
}