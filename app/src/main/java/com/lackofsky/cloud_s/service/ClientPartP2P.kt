package com.lackofsky.cloud_s.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Metadata
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.model.TransportData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.jmdns.impl.DNSRecord.IPv6Address

class ClientPartP2P @Inject constructor(
    private val gson: Gson,
    private val userRepository: UserRepository,
    private val metadata: Metadata
) {
    // Поток данных для обмена между компонентами
    private val TAG = "GrimBerry P2P (clientPart)"
    val discoveredPeers = MutableStateFlow<MutableSet<Peer>>(mutableSetOf())

    private val _activeFriends = MutableStateFlow<MutableMap<User, NettyClient>>(mutableMapOf())
    val activeFriends: StateFlow<Map<User, NettyClient>> = _activeFriends
    private val _activeStrangers = MutableStateFlow<MutableMap<User, NettyClient>>(mutableMapOf())
    val activeStrangers: StateFlow<Map<User, NettyClient>> = _activeStrangers

    /***
     * outgoing requests */
    private val _requestedStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val requestedStrangers: StateFlow<Set<User>> = _requestedStrangers
    /***
     * incoming requests */
    private val _pendingStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val pendingStrangers: StateFlow<Set<User>> = _pendingStrangers

    /*** добавить флоу пиров (friends+strangers)
     *
     *  сервер -- принимает whoami - передает пользователя сюда. на onRemove мы его изымаем
     *
     *
     * активные - друзья,
     *            посторонние
     *
     * */




    fun addPendingStranger(user: User) {
        _pendingStrangers.update {
            it.toMutableSet().let{
                it.add(user)
                it
            }
        }
//        _pendingStrangers.value.add(user)
    }

    fun removePendingStranger(user: User) {
        _pendingStrangers.update {
            it.toMutableSet().let{
                it.remove(user)
                it
            }
        }
//        _pendingStrangers.value.remove(user)
    }

    fun addRequestedStranger(user: User) {
        _requestedStrangers.update {
            it.toMutableSet().let{
                it.add(user)
                it
            }
        }
//        _requestedStrangers.value.add(user)
    }

    fun removeRequestedStranger(user: User) {
        _requestedStrangers.update {
            it.toMutableSet().let{
                it.removeIf( { it.uniqueID == user.uniqueID })
                it
            }
        }
        //_requestedStrangers.value.remove(user)
    }

    //lateinit var userOwner: MutableLiveData<User>
    private val _userOwner = MutableStateFlow<User?>(null)
    val userOwner: StateFlow<User?> get() = _userOwner
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> get() = _userInfo

    init {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.getUserOwner().collect { user ->
                _userOwner.value = user
                user.let {
                    val info = userRepository.getUserInfoById(it.uniqueID).firstOrNull()
                    _userInfo.value = info
                }
            }





            //CoroutineScope(Dispatchers.IO).launch {
//            userOwner =
//                userRepository.getUserOwner() //TODO( ISSUE:при смене данных о пользователе будут отправлятся изначальные данные  bad flow)
            //userInfo = userRepository.getUserInfoById(userOwner.value!!.id)
//                userRepository.getUserOwner().observeForever { user ->
//                    _userOwner.value = user
//                    //TODO("добавить send whoami при изменении данных")
//            }

            // }
        }
    }
        //    userOwner.value?.uniqueID.let{//todo delete
//        userRepository.getUserInfoById(it).observeForever(){
//            userInfo = it
//        }
//    }
        fun addActiveUser(user: User) {
            CoroutineScope(Dispatchers.IO).launch {
                val client = NettyClient(user.ipAddr, user.port)
                client.connect(
                    removeActiveUserCallback = {
                    removeActiveUser(
                        Peer(name = "", address = user.ipAddr)
                    )
                Log.i(
                    "service $SERVICE_NAME :client",
                    "removeActiveUserCallback. Connection closed: ${user.ipAddr + user.port}"
                )
            }
            )
                if (userRepository.getUserByUniqueID(user.uniqueID).firstOrNull() != null) {
                    /*** якщо користувач вже є в БД, т.е. - вже є в друзях
                     * оновлюємо користувача в базі данних
                     * додаємо до "друзів онлайн"
                     * */
                    //val updateUser = existingFriend.copy(fullName = user.fullName, login = user.login)
                    userRepository.updateUser(user)//updateUser) //TODO check
                    _activeFriends.update {currentMap ->
                        currentMap.toMutableMap().apply {
                            put(user, client)
                        }
                    }
                } else {
                    _activeStrangers.update {currentMap ->
                        currentMap.toMutableMap().apply {
                            put(user, client)
                        }
                    }
                }

            }

        }
        fun addStrangerToFriend(userUniqueId: String){
            CoroutineScope(Dispatchers.IO).launch {
                var userToRemove: User? = null
                var client: NettyClient? = null

                _activeStrangers.update { users ->
                    userToRemove = users.keys.find { it.uniqueID == userUniqueId }
                    Log.d("Service $SERVICE_NAME :client", "addStrangerToFriend0: ${users.keys} .userToRemove = $userToRemove")
                    users.toMutableMap().apply {
                        client = remove(userToRemove)
                    }
                }
                try {
                    _activeFriends.update { friends ->
                        friends.toMutableMap().apply {
                            put(userToRemove!!, client!!)
                        }
                    }
                    Log.d("Service $SERVICE_NAME :client", "123" +_activeFriends.value.toString())
                    Log.d("Service $SERVICE_NAME :client", "123" + userToRemove.toString() +" " + client.toString())
                } catch (e: Exception) {
                    Log.d(
                        "service $SERVICE_NAME :client",
                        "addStrangerToFriend3: exception $e .user = $userToRemove, client = $client"
                    )
                }

            }
        }
    fun deleteFriendToStranger(userUniqueId: String){
        CoroutineScope(Dispatchers.IO).launch {
            var userToRemove: User? = null
            var client: NettyClient? = null

            _activeFriends.update { users ->
                userToRemove = users.keys.find { it.uniqueID == userUniqueId }
                users.toMutableMap().apply {
                    client = remove(userToRemove)

                }
            }
            try {
                _activeStrangers.update { friends ->
                    friends.toMutableMap().apply {
                        put(userToRemove!!, client!!)
                    }
                }
            } catch (e: Exception) {
                Log.d(
                    "service $SERVICE_NAME :client",
                    "addStrangerToFriend: exception $e .user = $userToRemove, client = $client"
                )
            }
        }
    }

        fun removeActiveUser(peer: Peer) {
            CoroutineScope(Dispatchers.IO).launch {
                _activeFriends.update { currentMap ->

                    val userToRemove = currentMap.keys.find { it.ipAddr == peer.address }
                    currentMap.toMutableMap().apply {
                        remove(userToRemove)?.close()
                    }
                }
                _activeStrangers.update { currentMap ->
                    val userToRemove = currentMap.keys.find { it.ipAddr == peer.address }
                    currentMap.toMutableMap().apply {
                        remove(userToRemove)?.close()
                    }
                }
            }

        }

        fun sendMessage(activeFriend: User, message: Message): Boolean {

            val client = _activeFriends.value.entries.find { it.key.uniqueID == activeFriend.uniqueID }?.value

            if (client != null) {
                val content = gson.toJson(message)
                val sender = gson.toJson(
                    userOwner.value           //!!.copy(port = 123)//setting server port
                )
                val transportData = TransportData(
                    messageType = MessageType.MESSAGE,
                    senderId = activeFriend.uniqueID,
                    sender = sender,
                    content = content
                )
                val json = gson.toJson(transportData)
                client.sendMessage(json)
            } else {
                //throw Exception("BerryGrim. Attempt to send message to non-active channel.. P2PClient")
                return false
            }
            return true
        }

        fun addFriendInfo(userInfo: UserInfo) {
            TODO()
        }

        fun sendWhoAmI(host: String, targetPort: Int, ownPort: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("service $SERVICE_NAME :client", "sending Who Am I")
                val client = NettyClient(host, targetPort)
                try {
                    client.connect()
                    val content = gson.toJson(userOwner.value)
                    val sender = gson.toJson(userOwner.value)
                    val transportData = TransportData(
                        messageType = MessageType.USER_CONNECT,
                        senderId = userOwner.value!!.uniqueID,
                        ownServerPort = ownPort,
                        sender = sender,
                        content = content
                    )
                    val json = gson.toJson(transportData)
                    delay(1000)
                    client.sendMessage(json)
                    Log.d("service $SERVICE_NAME :client", "SENDED $json")
                } catch (e: Exception) {
                    Log.d("service $SERVICE_NAME :client", "catched $e")
                } finally {
                    delay(3000)
                    client.close()
                }
            }
        }
    fun onDestroy(info: String){
        CoroutineScope(Dispatchers.IO).launch {
        _activeFriends.update { map ->
            map.toMutableMap().apply {
                forEach( { (_, client) -> client.close()})
                clear()
            }
        }
        _activeStrangers.update { map ->
            map.toMutableMap().apply {
                forEach( { (_, client) -> client.close()})
                clear()
            }
        }
        Log.i("service $SERVICE_NAME :client", "onDestroy: $info")
}
    }
}