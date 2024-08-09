package com.lackofsky.cloud_s.services.p2pService

import android.content.Context
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import com.adroitandroid.near.model.Host
import com.lackofsky.cloud_s.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class P2pByNear @Inject constructor(private val context: Context) {
    private lateinit var nearDiscovery: NearDiscovery
    private lateinit var nearConnect: NearConnect
    private var isDiscovering = false

    private val _hosts = MutableStateFlow<Set<Host>>(mutableSetOf())
    val hosts: StateFlow<Set<Host>> get() = _hosts.asStateFlow()


    private val nearDiscoveryListener: NearDiscovery.Listener
        get() = object : NearDiscovery.Listener {
            override fun onPeersUpdate(hosts: Set<Host>) {
                _hosts.value = hosts
                for(host in hosts){
                    nearConnect.send(MESSAGE_REQUEST_WHO_ARE_U.toByteArray(), host)
                }
                //todo изменить список найденых пользователей
                //todo тем же образом изменять статус активности пользователей-друзей
            }

            override fun onDiscoveryTimeout() {
                //todo выдавать уведомление о прошествии времени поиска (пользователи не найдены
                isDiscovering = false
            }

            override fun onDiscoveryFailure(e: Throwable) {
                //todo
//                Snackbar.make(binding.root,
//                    "Something went wrong while searching for participants",
//                    Snackbar.LENGTH_LONG).show()
            }

            override fun onDiscoverableTimeout() {
                //todo уведомление, что ваша видимость для других пользователей - отключена
            }
        }

    private val nearConnectListener: NearConnect.Listener
        get() = object : NearConnect.Listener {
            override fun onReceive(bytes: ByteArray, sender: Host) {
                when (String(bytes)) {
                    MESSAGE_REQUEST_START_CHAT -> {
                            nearConnect.send(MESSAGE_RESPONSE_ACCEPT_REQUEST.toByteArray(), sender)
                        //nearConnect.send(MESSAGE_RESPONSE_DECLINE_REQUEST.toByteArray(), sender) }.create().show()
                            stopNearServicesAndStartChat(sender)
                            }
                    MESSAGE_RESPONSE_DECLINE_REQUEST -> {}
                    MESSAGE_RESPONSE_ACCEPT_REQUEST -> stopNearServicesAndStartChat(sender)
                    MESSAGE_REQUEST_WHO_ARE_U -> {
                        nearConnect.send(User(fullName = "123").toString())
                    }
                }
            }

            override fun onSendComplete(jobId: Long) {}
            override fun onSendFailure(e: Throwable?, jobId: Long) {}
            override fun onStartListenFailure(e: Throwable?) {}
        }

    private fun stopNearServicesAndStartChat(sender: Host) {
        nearConnect.stopReceiving(true)
        nearDiscovery.stopDiscovery()
//        ChatActivity.start(this@MainActivity, sender)
    }

    fun runService(){
        nearDiscovery = NearDiscovery.Builder()
            .setContext(context)
            .setDiscoverableTimeoutMillis(DISCOVERABLE_TIMEOUT_MILLIS)
            .setDiscoveryTimeoutMillis(DISCOVERY_TIMEOUT_MILLIS)
            .setDiscoverablePingIntervalMillis(DISCOVERABLE_PING_INTERVAL_MILLIS)
            .setDiscoveryListener(nearDiscoveryListener, Looper.getMainLooper())
            .build()

        nearConnect = NearConnect.Builder()
            .fromDiscovery(nearDiscovery)
            .setContext(context)
            .setListener(nearConnectListener, Looper.getMainLooper())
            .build()
    }
     fun stopService() {
         nearDiscovery.stopDiscovery()
         nearConnect.stopReceiving(true)
     }

    private fun stopDiscovery() {
        nearDiscovery.stopDiscovery()
        nearDiscovery.makeNonDiscoverable()
        isDiscovering = false
        //todo impl
    }

    private fun startDiscovery() {
        isDiscovering = true
        nearDiscovery.startDiscovery()
        //todo impl
    }





    companion object {
        private const val DISCOVERABLE_TIMEOUT_MILLIS: Long = 60000
        private const val DISCOVERY_TIMEOUT_MILLIS: Long = 10000
        private const val DISCOVERABLE_PING_INTERVAL_MILLIS: Long = 5000
        const val MESSAGE_REQUEST_START_CHAT = "start_chat"
        const val MESSAGE_RESPONSE_DECLINE_REQUEST = "decline_request"
        const val MESSAGE_RESPONSE_ACCEPT_REQUEST = "accept_request"
        const val MESSAGE_REQUEST_WHO_ARE_U = "WHO_ARE_U"
    }
}