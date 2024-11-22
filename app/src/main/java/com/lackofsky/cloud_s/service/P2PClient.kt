package com.lackofsky.cloud_s.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.model.TransportData
import dagger.hilt.android.AndroidEntryPoint
import io.netty.channel.Channel
import javax.inject.Inject

//@AndroidEntryPoint
//class P2PClient @Inject constructor(
//    private val gson: Gson,
//    private val userRepository: UserRepository
//): ClientInterface {
//    private val activeConnections = mutableMapOf<User, NettyClient>()
//
//
////    private fun sendUser(clientName: String):Boolean{
////        try{
////            val userOwner = userRepository.getUserOwner()
////            val nettyClient = clients.get(clientName)
////            val contentUser = gson.toJson(
////                userOwner
////            )
////            //send User
////            var transportData = TransportData(
////                messageType = MessageType.USER,
////                senderId = "",
////                content = contentUser)
////            nettyClient!!.sendMessage(
////                gson.toJson(transportData)
////            )
////            //send userInfo
////            val userInfo = userRepository.getUserInfoById(userOwner.value!!.id)
////            val contentUserInfo = gson.toJson(userInfo)
////            transportData = TransportData(
////                messageType = MessageType.USER_INFO,
////                senderId = "",
////                content = contentUserInfo)
////            nettyClient.sendMessage(
////                gson.toJson(transportData)
////            )
////        }catch(e: Exception){
////            return false
////        }
////        return true
////    }
////    private fun sendUserInfo(clientName: String):Boolean{
////        try{
////            val userInfo = userRepository.getUserOwner().value!!.id
////            val nettyClient = clients.get(clientName)
////            val contentUserInfo = gson.toJson(userInfo)
////
////            val transportData = TransportData(
////                messageType = MessageType.USER_INFO,
////                senderId = "",
////                content = contentUserInfo)
////            nettyClient!!.sendMessage(
////                gson.toJson(transportData)
////            )
////        }catch(e: Exception){
////            return false
////        }
////        return true
////    }
////    override fun connectToPeer(peer: Peer){//TODO basic implementation
////        //clients[peer.name] = NettyClient(host = peer.address, port = peer.port)
////
////        sendUser(peer.name)
////        sendUserInfo(peer.name)
////        //TODO подвязать передачу данных о пользователе
////    }
//    override fun connectTo(activeFriend: User) {
//        val client = NettyClient(activeFriend.ipAddr,activeFriend.port)
//        client.connect()
//        activeConnections.put(activeFriend,client)
//    }
//    override fun disconnect(activeFriend: User) {
//        val client = activeConnections.get(activeFriend)
//        client?.close()
//        activeConnections.remove(activeFriend)
//    }
//     override fun sendMessage(activeFriend: User, message: Message):Boolean{
//        val client = activeConnections.get(activeFriend)
//         if(client !=null){
//             val content = gson.toJson(message)
//             val transportData = TransportData(
//                 messageType = MessageType.MESSAGE,
//                 senderId = activeFriend.uniqueID,
//                 senderIp = "",
//                 content = content
//             )
//             val json = gson.toJson(transportData)
//             client.sendMessage(json)
//         }else{
//             //throw Exception("BerryGrim. Attempt to send message to non-active channel.. P2PClient")
//             return false
//         }
//        return true
//    }
//
//}
