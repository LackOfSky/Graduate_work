package com.lackofsky.cloud_s.services.p2pService

import android.content.Context
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.adroitandroid.near.connect.NearConnect
import com.adroitandroid.near.discovery.NearDiscovery
import com.adroitandroid.near.model.Host
import com.lackofsky.cloud_s.data.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class Near @Inject constructor(
    private val discoveryByNear: DiscoveryByNear,
    private val connectByNear: ConnectByNear
) {
    private val strangersHosts: StateFlow<List<Host>> = discoveryByNear.strangersHosts

    val friends: StateFlow<List<HostUser>> = discoveryByNear.friends
    val strangers: StateFlow<List<HostUser>> = connectByNear.strangers

    private val _isWorking = MutableLiveData<Boolean>(false)
    val isWorking : LiveData<Boolean> = _isWorking
    init{
        CoroutineScope(Dispatchers.IO).launch {
            strangersHosts.collect { newStrangers ->
                newStrangers.forEach { host ->connectByNear.sendWHO_ARE_YOU(host) }
            }
        }
    }

    fun start(){
        discoveryByNear.startDiscovery()
        connectByNear.startReceiving(discoveryByNear.getDiscovery())
        _isWorking.value = true
    }
    fun stop(){
        connectByNear.stopReceiving()
        discoveryByNear.stopDiscovery()
        _isWorking.value = false
    }
    /**
     * Во избежание лишних зависимостей просто импортировать ConnectByNear для обмена сообщениями
     * */
    fun send(host: Host,message: Message){
        connectByNear.send(host,message)
    }
    fun sendGET_INFO(host: Host){
        connectByNear.sendGET_INFO(host)
    }

}