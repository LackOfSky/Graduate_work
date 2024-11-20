package com.lackofsky.cloud_s.service.server.discovery

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.Peer
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.ui.ShowToast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class WiFiDirectManager @Inject constructor(private val applicationContext: Context,
                                            private val clientPartP2P: ClientPartP2P) {
    val discoveryState = MutableStateFlow(DiscoveryState.STOPPED)
    private val manager: WifiP2pManager by lazy {
        applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val channel: WifiP2pManager.Channel by lazy {
        manager.initialize(applicationContext, applicationContext.mainLooper, null)
    }

//    private val _peers = MutableStateFlow<Set<WifiP2pDevice>>(emptySet()) // Реактивное состояние списка устройств
//    val peers: StateFlow<Set<WifiP2pDevice>> = _peers
    private val _peers = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val peers: StateFlow<List<WifiP2pDevice>> = _peers
    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        //TODO("отследить работоспособность ")
        val currentPeers = _peers.value
        val newPeers = peerList.deviceList.filterNot { device ->
            Log.d("service GrimBerry device", device.toString())
            sendToastIntend(device.toString())
            currentPeers.any { it.deviceAddress == device.deviceAddress }
        }
        if (newPeers.isNotEmpty()) {
            // Добавляем только новые устройства и обрабатываем их
            _peers.value = currentPeers + newPeers
            newPeers.forEach { device ->
                connectToPeer(device)
            }
        }
    }
    private val wifiP2pReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d("WifiDirectService GrimBerry", "Wi-Fi Direct is enabled")
                        if(discoveryState.value.equals(DiscoveryState.STOPPED)){
                            startPeerDiscovery()
                        }
                        //sendToastIntend("Wi-Fi Direct is enabled ")
                    } else {
                        Log.d("WifiDirectService GrimBerry", "Wi-Fi Direct is disabled ")
                        if(discoveryState.value != DiscoveryState.DISABLED){
                            stopPeerDiscovery()
                            discoveryState.value = DiscoveryState.DISABLED
                        }
                        sendToastIntend("Discovery is disabled. Please, turn on your Wi-Fi")
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (checkPermission()) {
                        manager.requestPeers(channel, peerListListener)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // обработка события изменения состояния соединения
                    Log.d("WifiDirectService GrimBerry", "Wi-Fi Direct connection state changed")
                    sendToastIntend("Wi-Fi Direct connection state changed")
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.d("WifiDirectService GrimBerry", "Device configuration changed")
                    sendToastIntend("Device configuration changed")
                }
            }
        }
    }

     fun startPeerDiscovery() {
         // Регистрируем широковещательные намерения для Wi-Fi Direct событий
         applicationContext.registerReceiver(wifiP2pReceiver, intentFilter)
         if (checkPermission()) {
             manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                 override fun onSuccess() {
                     Log.d("WifiDirectService GrimBerry", "Peer discovery started")
                     sendToastIntend("Peer discovery started")
                     discoveryState.value = DiscoveryState.WORKING
                 }

                 override fun onFailure(reason: Int) {
                     Log.e("WifiDirectService GrimBerry", "Peer discovery failed: $reason")
                     sendToastIntend("Peer discovery failed: $reason. Turn on a Wi-Fi + geolocation")
                     //toast включите wifi + геолокацию
                     discoveryState.value = DiscoveryState.DISABLED

                 }

             })
             manager.requestPeers(channel, peerListListener)
         }

    }
    fun stopPeerDiscovery(){
        applicationContext.unregisterReceiver(wifiP2pReceiver)
        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("WifiDirectService GrimBerry", "Peer discovery stopped")
                sendToastIntend("Peer discovery stopped")
                discoveryState.value = DiscoveryState.STOPPED
            }

            override fun onFailure(reason: Int) {
                Log.e("WifiDirectService GrimBerry", "Stop peer discovery failed: $reason")
                sendToastIntend("Stop peer discovery failed: $reason")
                discoveryState.value = DiscoveryState.DISABLED
            }
        })
    }
    fun connectToPeer(device: WifiP2pDevice) {
        /*** Подключается к конкретному устройству. Соединение устанавливается с помощью onPeerResolved.
         *   Обработка разрыва соединения производится серверной частью. NettyServer */
        if (!checkPermission()) {
            throw Exception("wifiDirectManager GrimBerry: permission is not granted")//return
        }
            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
                wps.setup = WpsInfo.PBC

            }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {//prev = manager.connect
            override fun onSuccess() {
                Log.d("WifiDirectService GrimBerry", "Connection initiated with ${device.deviceName}")
                sendToastIntend("Connection initiated with ${device.deviceName}")
                clientPartP2P.sendWhoAmI(device.deviceAddress)
            }

            override fun onFailure(reason: Int) {
                Log.e("WifiDirectService GrimBerry", "Connection failed: $reason")
                sendToastIntend("Connection failed: $reason")
            }
        })
//            manager.createGroup(channel, config, object : WifiP2pManager.ActionListener {//prev = manager.connect
//                override fun onSuccess() {
//                    Log.d("WifiDirectService GrimBerry", "Connection initiated with ${device.deviceName}")
//                    sendToastIntend("Connection initiated with ${device.deviceName}")
//                    clientPartP2P.sendWhoAmI(device.deviceAddress)
//                }
//
//                override fun onFailure(reason: Int) {
//                    Log.e("WifiDirectService GrimBerry", "Connection failed: $reason")
//                    sendToastIntend("Connection failed: $reason")
//                }
//            })
    }

    private fun checkPermission():Boolean{
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw Exception("wifi direct manager GrimBerry: permissions is not granted")

        }else{
            return true
        }
    }
    private fun sendToastIntend(message: String){
        val intent = Intent("com.lackofsky.cloud_s.SHOW_TOAST")
        intent.putExtra("message", message)
        applicationContext.sendBroadcast(intent)
    }
}