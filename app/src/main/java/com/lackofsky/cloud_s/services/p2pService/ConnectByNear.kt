package com.lackofsky.cloud_s.services.p2pService

import android.content.Context
import android.os.Looper
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import com.adroitandroid.near.model.Host
import com.lackofsky.cloud_s.data.model.Message
import javax.inject.Inject
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import java.net.InetAddress

class ConnectByNear @Inject constructor(private val context: Context,userRepository: UserRepository) {
    private lateinit var nearConnect: NearConnect

    private val _strangers = MutableStateFlow<MutableList<HostUser>>(mutableListOf<HostUser>())
    val strangers: StateFlow<MutableList<HostUser>> get() = _strangers.asStateFlow()

    val gson = Gson()
    private val nearConnectListener = object : NearConnect.Listener {
        override fun onReceive(bytes: ByteArray, sender: Host) {
            when (val data = String(bytes)) {
                MESSAGE_REQUEST_START_CHAT -> {
                    nearConnect.send(MESSAGE_RESPONSE_ACCEPT_REQUEST.toByteArray(), sender)
                    stopNearServicesAndStartChat(sender)
                }
                MESSAGE_RESPONSE_DECLINE_REQUEST -> {}
                MESSAGE_RESPONSE_ACCEPT_REQUEST -> stopNearServicesAndStartChat(sender)
                MESSAGE_REQUEST_WHO_ARE_U -> {//USER info мы не получаем
                    nearConnect.send((MESSAGE_RESPONSE_PREFIX_I_AM_+gson
                        .toJson(userRepository.getUserOwner().value)).toByteArray(), sender)
                }
                else ->{
                    if(data.startsWith(MSG_PREFIX)){
                        val message = gson.fromJson(data.substring(4), Message::class.java)
                        //todo В бд message
                    }else if(data.startsWith(MESSAGE_RESPONSE_PREFIX_I_AM_)){
                        val user = gson.fromJson(data.substring(4), User::class.java)
                        val hostUser = HostUser(sender,user)
                        val prevSet = _strangers.value.toMutableList()
                        prevSet.add(hostUser)
                        _strangers.value =  prevSet
                    }else if(data.startsWith(MESSAGE_RESPONSE_PREFIX_GET_INFO)){
                        val userInfo = gson.fromJson(data.substring(4), UserInfo::class.java)
                        _strangers.value.forEach {
                            hostUser -> if(hostUser.host.hostAddress == sender.hostAddress){
                                hostUser.userInfo = userInfo
                                return@forEach
                            }
                        }
                    }

                }
            }
        }

        override fun onSendComplete(jobId: Long) {}
        override fun onSendFailure(e: Throwable?, jobId: Long) {}
        override fun onStartListenFailure(e: Throwable?) {}
    }

    private fun stopNearServicesAndStartChat(sender: Host) {
        nearConnect.stopReceiving(true)
        // Start chat activity or other action
    }

    fun startReceiving(discovery: NearDiscovery) {
        nearConnect = NearConnect.Builder()
            .fromDiscovery(discovery)
            .setContext(context)
            .setListener(nearConnectListener, Looper.getMainLooper())
            .build()
        _strangers.value = getFriendList()
    }
    fun send(host: Host,message: Message){
        nearConnect.send((MSG_PREFIX+gson.toJson(message)).toByteArray(), host)
    }
    fun sendWHO_ARE_YOU(host: Host){
        nearConnect.send(MESSAGE_REQUEST_WHO_ARE_U.toByteArray(), host)
    }
    fun sendGET_INFO(host: Host){
        nearConnect.send(MESSAGE_REQUEST_GET_INFO.toByteArray(),host)
    }

    fun stopReceiving() {
        nearConnect.stopReceiving(true)
    }
    companion object {
        private const val STATUS_TYPING = "status:typing"
        private const val STATUS_STOPPED_TYPING = "status:stopped_typing"
        private const val STATUS_EXIT_CHAT = "status:exit_chat"
        const val MSG_PREFIX = "msg:"
        const val MESSAGE_REQUEST_START_CHAT = "start_chat"
        const val MESSAGE_RESPONSE_DECLINE_REQUEST = "decline_request"
        const val MESSAGE_RESPONSE_ACCEPT_REQUEST = "accept_request"
        const val MESSAGE_REQUEST_WHO_ARE_U = "WHO_ARE_U" //+
        const val MESSAGE_RESPONSE_PREFIX_I_AM_ = "ima:"  //+
        const val MESSAGE_REQUEST_GET_INFO = "GET_INFO"
        const val MESSAGE_RESPONSE_PREFIX_GET_INFO = "inf:"
    }



//MOCK
    var _x = MutableStateFlow<HostUser>(
        HostUser(Host(InetAddress.getLoopbackAddress(),"23","3"),
               User(1,"John Doe", //TODO подхват с БД
            "@just_someone","03030330")
        )
    )
    val currentUser : StateFlow<HostUser> = _x
    fun getFriendList():MutableList<HostUser>{
        val list = arrayListOf(
            currentUser.value, currentUser.value, currentUser.value,
            currentUser.value, currentUser.value, currentUser.value,
            currentUser.value, currentUser.value, currentUser.value
        )
        return list
    }
}
