package com.lackofsky.cloud_s.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.client.ClientInterface
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.model.TransportData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class P2PClient @Inject constructor(
    private val gson: Gson,
    private val userRepository: UserRepository
): Service(), ClientInterface {
    private val binder = LocalBinder()
    private val clients = mutableMapOf<String,NettyClient>()


    override fun onDestroy() {
        clients.forEach{
            (key, client) -> client.close()
        }
        super.onDestroy()
    }

    private fun sendUser(clientName: String):Boolean{
        try{
            val userOwner = userRepository.getUserOwner()
            val nettyClient = clients.get(clientName)
            val contentUser = gson.toJson(
                userOwner
            )
            //send User
            var transportData = TransportData(
                messageType = MessageType.USER,
                senderId = "",
                content = contentUser)
            nettyClient!!.sendMessage(
                gson.toJson(transportData)
            )
            //send userInfo
            val userInfo = userRepository.getUserInfoById(userOwner.value!!.id)
            val contentUserInfo = gson.toJson(userInfo)
            transportData = TransportData(
                messageType = MessageType.USER_INFO,
                senderId = "",
                content = contentUserInfo)
            nettyClient.sendMessage(
                gson.toJson(transportData)
            )
        }catch(e: Exception){
            return false
        }
        return true
    }
    private fun sendUserInfo(clientName: String):Boolean{
        try{
            val userInfo = userRepository.getUserOwner().value!!.id
            val nettyClient = clients.get(clientName)
            val contentUserInfo = gson.toJson(userInfo)

            val transportData = TransportData(
                messageType = MessageType.USER_INFO,
                senderId = "",
                content = contentUserInfo)
            nettyClient!!.sendMessage(
                gson.toJson(transportData)
            )
        }catch(e: Exception){
            return false
        }
        return true
    }

    override fun connectToPeer(peer: Peer){//TODO basic implementation
        //clients[peer.name] = NettyClient(host = peer.address, port = peer.port)

        sendUser(peer.name)
        sendUserInfo(peer.name)
        //TODO подвязать передачу данных о пользователе
    }
    override fun sendMessage(clientName: String, message: Message):Boolean{
        try{
            val nettyClient = clients.get(clientName)
            val contentMessage = gson.toJson(message)

            val transportData = TransportData(
                messageType = MessageType.MESSAGE,
                senderId = "",
                content = contentMessage)
            nettyClient!!.sendMessage(
                gson.toJson(transportData)
            )
        }catch(e: Exception){
            return false
        }
        return true
    }



    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
    inner class LocalBinder : Binder() {
        fun getService(): P2PClient = this@P2PClient
    }
}