package com.lackofsky.cloud_s.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.model.TransportData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ClientPartP2P @Inject constructor(
    private val gson: Gson,
    private val userRepository: UserRepository,
    private val metadata: Metadata
) {
    // Поток данных для обмена между компонентами

    private val _activeFriends = MutableStateFlow<MutableMap<User, NettyClient>>(mutableMapOf())
    val activeFriends: StateFlow<MutableMap<User, NettyClient>> = _activeFriends
    private val _activeStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val activeStrangers: StateFlow<MutableSet<User>> = _activeStrangers
    /***
     * outgoing requests */
    private val _requestedStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val requestedStrangers: StateFlow<MutableSet<User>> = _requestedStrangers
    /***
     * incoming requests */
    private val _pendingStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val pendingStrangers: StateFlow<MutableSet<User>> = _pendingStrangers

    /*** добавить флоу пиров (friends+strangers)
     *
     *  сервер -- принимает whoami - передает пользователя сюда. на onRemove мы его изымаем
     *
     *
     * активные - друзья,
     *            посторонние
     *
     * */
    fun addPendingStranger(user: User){
        _pendingStrangers.value.add(user)
    }
    fun removePendingStranger(user: User){
        _pendingStrangers.value.remove(user)
    }
    fun addRequestedStranger(user: User){
        _requestedStrangers.value.add(user)
    }
    fun removeRequestedStranger(user: User){
        _requestedStrangers.value.remove(user)
    }

    //lateinit var userOwner: MutableLiveData<User>
    private val _userOwner = MutableLiveData<User>()
    val userOwner: LiveData<User> get() = _userOwner
    lateinit var userInfo :LiveData<UserInfo>
    init{
        //CoroutineScope(Dispatchers.IO).launch {
//            userOwner =
//                userRepository.getUserOwner() //TODO( ISSUE:при смене данных о пользователе будут отправлятся изначальные данные  bad flow)
            //userInfo = userRepository.getUserInfoById(userOwner.value!!.id)
                userRepository.getUserOwner().observeForever { user ->
                    _userOwner.value = user
                    //TODO("добавить send whoami при изменении данных")
            }
       // }
    }

    suspend fun addActiveUser(user: User){
        if(userRepository.getUserByUniqueID(user.uniqueID).isInitialized){
            userRepository.updateUser(user)
            val client = NettyClient(user.ipAddr,user.port)
            _activeFriends.value.put(user, client)
//            client.connect()
        }else{
            _activeStrangers.value.add(user)
        }
    }

     fun removeActiveUser(peer: Peer) {
         _activeFriends.update { users ->
             val userToRemove = users.keys.find { it.ipAddr == peer.address }
             if (userToRemove != null) {
                 users.remove(userToRemove)
                     ?.close()
             }else{
                 throw Exception("GrimBerry. Attempt to delete user that doesn't exist. sharedState")
             }
             users
         }
            _activeStrangers.update { users ->
                users.removeIf { it.ipAddr == peer.address }
                users
            }

    }
     fun sendMessage(activeFriend: User, message: Message):Boolean{
        val client = _activeFriends.value.get(activeFriend)
        if(client !=null){
            val content = gson.toJson(message)
            val sender = gson.toJson(userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.MESSAGE,
                senderId = activeFriend.uniqueID,
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

    fun addFriendInfo(userInfo: UserInfo){
        TODO()
    }
    fun sendWhoAmI(addr: String){
        val client = NettyClient(addr, metadata.defaultPort)//
        try {
            client.connect()
            Log.d("service $SERVICE_NAME :client", "connected")
            val content = gson.toJson(userOwner.value)
            val sender = gson.toJson(userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.USER,
                senderId = userOwner.value!!.uniqueID,
                senderIp = "",
                sender= sender,
                content = content
            )
            val json = gson.toJson(transportData)
            client.sendMessage(json)
            Log.d("service $SERVICE_NAME :client", "SENDED $json")
        }catch (e: Exception){
            Log.d("service $SERVICE_NAME :client", "catched $e")
        }finally {
            Log.d("service $SERVICE_NAME :client", "finally ")
            client.close()
        }
    }
}