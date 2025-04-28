package com.lackofsky.cloud_s.service

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
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
import com.lackofsky.cloud_s.service.netty_media_p2p.NettyMediaClient
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val metadata: Metadata,
    private val mediaClient: NettyMediaClient
) {
    // Поток данных для обмена между компонентами
    private val TAG = "GrimBerry P2P (clientPart)"
    val discoveredPeers = MutableStateFlow<MutableSet<Peer>>(mutableSetOf())

    /*** Активні клієнти */
    private val _activeFriends = MutableStateFlow<MutableMap<User, NettyClient>>(mutableMapOf())
    val activeFriends: StateFlow<Map<User, NettyClient>> = _activeFriends
    private val _activeStrangers = MutableStateFlow<MutableMap<User, NettyClient>>(mutableMapOf())
    val activeStrangers: StateFlow<Map<User, NettyClient>> = _activeStrangers
    /*** */

    /*** Hot Stream of online\offline friends */
    private val _friendsOnline = MutableStateFlow(emptyList<User>())
    val friendsOnline : StateFlow<List<User>> get() = _friendsOnline

    private val _friendsOffline = MutableStateFlow(emptyList<User>())
    val friendsOffline : StateFlow<List<User>> get() = _friendsOffline

    /*** */

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

    private val _userOwner = MutableStateFlow<User?>(null)
    val userOwner: StateFlow<User?> get() = _userOwner
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> get() = _userInfo

    init {
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.getUserOwner().collect { user ->
                _userOwner.value = user
                    val info = userRepository.getUserInfoById(user.uniqueID).collect{userInfo ->
                        _userInfo.value = userInfo
                    }
            }
        }
        CoroutineScope(Dispatchers.IO).launch { /***_friendsOnline, _friendsOffline hot stream */
            userRepository.getAllUsers()
                .combine(activeFriends) { allUsers, activeFriends ->
                    val (online, offline) = allUsers.partition { user ->
                        Log.d("service GrimBerry :client", "Active friends emitted: ${user.toString()}")
                        activeFriends.keys.any{activeUser->activeUser.uniqueID == user.uniqueID}
                    }
                    Pair(online, offline)
                }
                .collect { (online, offline) ->
                    Log.d("service GrimBerry :client", "add active friend")
                    _friendsOnline.value = online
                    _friendsOffline.value = offline
                    Log.d("service GrimBerry :client", _friendsOnline.value.toString())
                }
        }
    }
        //    userOwner.value?.uniqueID.let{//todo delete
//        userRepository.getUserInfoById(it).observeForever(){
//            userInfo = it
//        }
//    }
        fun addActiveUser(user: User) {
            CoroutineScope(Dispatchers.IO).launch {
                val client = NettyClient(user.ipAddr, user.port)//nettyMediaClient
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
/*** takes user and send message by user uniqueId*/
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

    suspend fun sendMediaLogo(userUniqueId: String, port: Int){
        Log.d("service $SERVICE_NAME :client sendML", "sending")
        val client  = activeFriends.value.entries.find { it.key.uniqueID == userUniqueId }
        Log.d("service $SERVICE_NAME :client sendML", "userUniqueId = $userUniqueId, ${activeFriends.value.entries.toString()}")
        delay(1000)
        Log.d("service $SERVICE_NAME :client sendML", "sendMediaLogo ${userInfo.value!!.iconImgURI!!}")
                client?.let {
                    val ipAddr = it.value.getChannelIpAddress()
                    val isSuccess = mediaClient.sendUserLogoFile(uri = userInfo.value!!.iconImgURI!!.toUri(),
                        sender = userOwner.value!!,
                        serverIpAddr = ipAddr,
                        serverPort = port)
                    Log.d("service $SERVICE_NAME :client sendML", "sendMediaLogo ${userInfo.value!!.iconImgURI!!}")
                    if (isSuccess) {
                        Log.d("service $SERVICE_NAME :client sendML", "success")
                    } else {
                        Log.d("service $SERVICE_NAME :client sendML", "failed")
                    }
                }?: Log.e("service $SERVICE_NAME :client sendML", "client is null")


    }
    suspend fun sendMediaBanner(userUniqueId: String, port: Int){
        val client  = activeFriends.value.entries.find { it.key.uniqueID == userUniqueId }
        client?.let {
            val ipAddr = it.value.getChannelIpAddress()
            val isSuccess = mediaClient.sendUserBannerFile(uri = userInfo.value!!.bannerImgURI!!.toUri(),
                sender = userOwner.value!!,
                serverIpAddr = ipAddr,
                serverPort = port)
            if (isSuccess) {
                Log.d("service $SERVICE_NAME :client sendML", "success")
            } else {
                Log.d("service $SERVICE_NAME :client sendML", "failed")
            }
        }?: Log.e("service $SERVICE_NAME :client sendML", "client is null")
    }
    suspend fun sendMediaMessage(userUniqueId: String, port: Int, messageUniqueId: String){
        val client  = activeFriends.value.entries.find { it.key.uniqueID == userUniqueId }
        client?.let {
            val ipAddr = it.value.getChannelIpAddress()
            val isSuccess = mediaClient.sendMessageFile(messageUniqueId = messageUniqueId,
                sender = userOwner.value!!,
                serverIpAddr = ipAddr,
                serverPort = port)
            if (isSuccess) {
                Log.d("service $SERVICE_NAME :client sendML", "success")
            } else {
                Log.d("service $SERVICE_NAME :client sendML", "failed")
            }
        }?: Log.e("service $SERVICE_NAME :client sendML", "client is null")
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