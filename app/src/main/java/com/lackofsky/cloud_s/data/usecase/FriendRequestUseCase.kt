package com.lackofsky.cloud_s.data.usecase

import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Request
import com.lackofsky.cloud_s.service.model.TransportData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class FriendRequestUseCase @Inject constructor(val gson: Gson, val clientPartP2P: ClientPartP2P) {
    
    fun requestUserInfo(sendTo: User):Boolean {
        val client = clientPartP2P.activeStrangers.value.get(sendTo)
        if (client != null) {
            //val content = gson.toJson(clientPartP2P.userInfo.value)
            val sender = gson.toJson(clientPartP2P.userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.FRIEND_REQUEST_TYPE,
                senderId = clientPartP2P.userOwner.value!!.uniqueID,
                sender = sender,
                content = gson.toJson(FriendRequestType.USER_INFO)
            )
            val json = gson.toJson(transportData)
            client.sendMessage(json)
            return true
        }else{
            return false
        }
    }
}
enum class FriendRequestType {
    USER_INFO, DATA_CHANGED
}
enum class DataChanged{
    //todo создать notification "data changed" с указанием конкретных полей, что изменились.
    // по запросу пиров отправлять єти данные
}