package com.lackofsky.cloud_s.service.server.discovery

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager.EXTRA_WIFI_STATE
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer
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
    private val SERVICE_NAME = "cLoud_s"
    private val SERVICE_TYPE = "_GrimBerry._cLouds"
    private val discoveryTimeout = 10_000L //TODO("вынести выше")
    val discoveryState = MutableStateFlow(DiscoveryState.STOPPED)
    var isConnected = false
    var isGroupOwner = false
    private val serviceState = MutableStateFlow(false)
    private val manager: WifiP2pManager by lazy {
        applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }
    private val channel: WifiP2pManager.Channel by lazy {
        manager.initialize(applicationContext, applicationContext.mainLooper, null)
    }

    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }



//    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
//        try{
//            connectToGroup(
//                peerList.deviceList.first()
//            )
//        }catch (e:NoSuchElementException){
//            sendToastIntend(e.toString())
//            Log.d("WifiDirectService GrimBerry",e.toString())
//        }
//        //stopClientDiscovery()
//        discoveryState.value = DiscoveryState.STOPPED
//    }
    private val disconnectListener = object : WifiP2pManager.ActionListener {
        override fun onSuccess() {
            isConnected = false
            discoveryState.value = DiscoveryState.STOPPED
        }

        override fun onFailure(reason: Int) {
            TODO("handling exceptions reason $reason")
        }
    }


    private val wifiP2pReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d("WifiDirectService GrimBerry", "Wi-Fi Direct is enabled")
//                        if(discoveryState.value.equals(DiscoveryState.STOPPED)){
//                            startClientDiscovery()
//                            discoveryState.value = DiscoveryState.WORKING
//                        }
                        //sendToastIntend("Wi-Fi Direct is enabled ")
                    } else {
                        Log.d("WifiDirectService GrimBerry", "Wi-Fi Direct is disabled ")
                        if(discoveryState.value != DiscoveryState.DISABLED){
                            stopServiceDiscovery()
                            discoveryState.value = DiscoveryState.DISABLED
                        }
                        sendToastIntend("Discovery is disabled. Please, turn on your Wi-Fi")
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                    if (checkPermission()) {
                       // manager.requestPeers(channel, peerListListener)
                    }
                }
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    // обработка события изменения состояния соединения
//                    if (checkPermission()) {
//                        Log.d(
//                            "WifiDirectService GrimBerry",
//                            "Wi-Fi Direct connection state changed"
//                        )
//                        val connectivityManager =
//                            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                        val networkInfo = connectivityManager.activeNetworkInfo
//
//                        if (!networkInfo!!.isConnected) {
//                            start()
//                        }

                            // We are connected with the other device, request connection
                            // info to find group owner IP
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    Log.d("WifiDirectService GrimBerry", "Device configuration changed")
                    sendToastIntend("Device configuration changed")
                }
            }
        }
    }


    private fun startServiceDiscovery(){
        if (checkPermission()) {
            manager.setDnsSdResponseListeners(channel,
                { instanceName, serviceType, device ->
                    Log.d("GrimBerry manager setDnsSdResponseListeners", "Найдена служба: $instanceName ($serviceType) на устройстве ${device.deviceName}")
                    instanceName
                    if (instanceName.equals(SERVICE_NAME)){
                        clientPartP2P.discoveredPeers.value.add(Peer(instanceName,serviceType))
                        connectToGroup(device)
                    }

                },
                { fullDomainName, txtRecordMap, device ->
                    Log.d("GrimBerry manager setDnsSdResponseListeners", "Получены данные службы: $txtRecordMap на устройстве ${device.deviceName}")

                }
            )

        val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_TYPE)
        manager.addServiceRequest(channel, serviceRequest, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Запрос успешно добавлен
                Log.d("GrimBerry manager listener","addServiceRequest. Success")
                checkPermission()
                manager.discoverServices(channel,DefaultListener(
                    onSuccess = "service discovery started",
                    onFailure = "fail at discoverServices function",
                    applicationContext =  applicationContext)
                )
            }

            override fun onFailure(reason: Int) {
                // Ошибка добавления запроса
                Log.d("GrimBerry manager listener","addServiceRequest. onFailure. Reason: $reason")
            }
        })
            }
    }



//     private fun startServiceDiscovery() {
//         // Регистрируем широковещательные намерения для Wi-Fi Direct событий
//         checkPermission()
//              //discoverPeers  -> discoverServices
//             manager.discoverServices(channel, object : WifiP2pManager.ActionListener {
//                 override fun onSuccess() {
//                     Log.d("WifiDirectService GrimBerry", "Peer discovery started")
//                     sendToastIntend("Peer discovery started")
//                     discoveryState.value = DiscoveryState.WORKING
//                 }
//
//                 override fun onFailure(reason: Int) {
//                     Log.e("WifiDirectService GrimBerry", "Peer discovery failed: $reason")
//                     sendToastIntend("Peer discovery failed: $reason. Turn on a Wi-Fi + geolocation")
//                     //toast включите wifi + геолокацию
//                     discoveryState.value = DiscoveryState.DISABLED
//
//                 }
//             })
//    }
    private fun stopServiceDiscovery(){
        //applicationContext.unregisterReceiver(wifiP2pReceiver)
        manager.clearServiceRequests(channel,DefaultListener(
            onSuccess = "serviceRequests is cleared",
            onFailure = "failed to clear serviceRequests",
            applicationContext = applicationContext))
//        manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
//            override fun onSuccess() {
//                Log.d("WifiDirectService GrimBerry", "Peer discovery stopped")
//                sendToastIntend("Peer discovery stopped")
//                discoveryState.value = DiscoveryState.STOPPED
//            }
//
//            override fun onFailure(reason: Int) {
//                Log.e("WifiDirectService GrimBerry", "Stop peer discovery failed: $reason")
//                sendToastIntend("Stop peer discovery failed: $reason")
//                discoveryState.value = DiscoveryState.DISABLED
//            }
//        })
    }
    private fun connectToGroup(device: WifiP2pDevice) {//   connectToPeer
        /*** Подключается к конкретному устройству. Соединение устанавливается с помощью onPeerResolved.
         *   Обработка разрыва соединения производится серверной частью. NettyServer */
        checkPermission()

            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
                wps.setup = WpsInfo.PBC
            }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {//prev = manager.connect
            override fun onSuccess() {
                isConnected = true
                Log.d("WifiDirectService GrimBerry", "Connection initiated with ${device.deviceName}")
                sendToastIntend("Connection initiated with ${device.deviceName}")

                //manager.requestConnectionInfo migrated to listener

            }

            override fun onFailure(reason: Int) {
                Log.e("WifiDirectService GrimBerry", "Connection failed: $reason")
                sendToastIntend("Connection failed: $reason")
            }
        })

    }
    //createGroup -> startLocalService
    /***
     * Данная функция создает группу WiFi Direct и регистрирует наш сервис в сети Wi-Fi direct.
     * */
    private fun startLocalService() {
        CoroutineScope(Dispatchers.Main).launch {
            checkPermission()
            manager.createGroup(channel, object : WifiP2pManager.ActionListener {
                //prev = manager.connect
                override fun onSuccess() {
                    // Device is ready to accept incoming connections from peers.
                    Log.d("WifiDirectService GrimBerry", "Group is created")
                    addLocalService()
                    Log.d("WifiDirectService GrimBerry", "Server-cluster is up")
                    sendToastIntend("Server-cluster is up")
                    isGroupOwner = true
                }

                override fun onFailure(reason: Int) {

                    Log.e("WifiDirectService GrimBerry", "Connection failed: $reason. service is off")

                    val intent = Intent(applicationContext, P2PServer::class.java)
                    applicationContext.stopService(intent)
                    sendToastIntend("Group creation failed: $reason. Retry to start service")
                }
            })
        }
    }
    private fun addLocalService(){
        checkPermission()

            val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_NAME, SERVICE_TYPE,
                emptyMap() //mapOf("key1" to "value1", "key2" to "value2")
            )
            manager.addLocalService(
                channel, serviceInfo, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d("WifiDirectService GrimBerry","Local service added successfully")
                    }

                    override fun onFailure(reason: Int) {
                        Log.d("WifiDirectService GrimBerry","Failed to add local service")
                        throw Exception("failed to add Local service in WiFiDirectManager. addLocalService")
                    }
                }
            )

    }

    fun start(){
        //Handler(Looper.getMainLooper()).postDelayed({
        checkPermission()
        var isGroupCreated = false
        serviceState.value = true
        applicationContext.registerReceiver(wifiP2pReceiver, intentFilter)
        startServiceDiscovery()
        //manager.requestGroupInfo(channel){info ->info}
        manager.requestConnectionInfo(channel) { info ->

                // String from WifiP2pInfo struct
                //val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress
                if (info.groupFormed) {
                    isGroupCreated = true
                    isGroupOwner = info.isGroupOwner
                    if (info.isGroupOwner) {
                        // Do whatever tasks are specific to the group owner.
                        // One common case is creating a group owner thread and accepting
                        // incoming connections.
                        //Log.d("WiFiP2P", "сервер IP: ${info.groupOwnerAddress.hostAddress}")
                        Log.d("GrimmBerry WiFiP2P", "connected as GO")
                    } else{
                        // The other device acts as the peer (client). In this case,
                        // you'll want to create a peer thread that connects
                        // to the group owner.
                        //Log.d("WiFiP2P", "Это клиент. Сервер: ${info.groupOwnerAddress.hostAddress}")
                        Log.d("GrimmBerry WiFiP2P", "connected as participant")
                        clientPartP2P.sendWhoAmI(info.groupOwnerAddress.toString())
                    }
                }else{
                    Log.d("WifiDirectService", "Not connected to any group.")
                    isConnected = false
                }
            }
        if(isGroupCreated){
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                manager.removeGroup(channel, disconnectListener)
                delay(1000)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(discoveryTimeout)
            if (!isConnected) {
                startLocalService()
            }
        }
    }
    fun stop(){
        serviceState.value = false
        applicationContext.unregisterReceiver(wifiP2pReceiver)
        Log.d("WifiDirectService GrimBerry", "group owner "+isGroupOwner.toString())
        if(isGroupOwner){
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                manager.removeGroup(channel, disconnectListener)
                delay(1000)
            }
            isGroupOwner = false
        }else{
            manager.stopPeerDiscovery(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("WiFiDirectService", "Discovery stopped.")
                }

                override fun onFailure(reason: Int) {
                    Log.e("WiFiDirectService", "Failed to stop discovery: $reason")
                }
            })
            if(isConnected){ manager.cancelConnect(channel, disconnectListener) }
        }
    }

    private fun checkPermission():Boolean{
         val permissionsList = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
             Manifest.permission.ACCESS_NETWORK_STATE
         )

        val arePermissionsGranted = permissionsList.all { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if(arePermissionsGranted){
            return true
        }
        throw Exception("wifi direct manager GrimBerry: permissions is not granted")
    }
    private fun sendToastIntend(message: String){
        val intent = Intent("com.lackofsky.cloud_s.SHOW_TOAST")
        intent.putExtra("message", message)
        applicationContext.sendBroadcast(intent)
    }
}

class DefaultListener(val onSuccess: String, val showOnSuccess:Boolean=true,
                      val onFailure: String,val showOnFailure:Boolean=true,val applicationContext: Context) : WifiP2pManager.ActionListener{
    private val tag: String = "GrimBerry manager listener"
    override fun onSuccess() {
        if(showOnSuccess){
            sendToastIntend(onSuccess)
        }
        Log.d(tag,onSuccess)
    }

    override fun onFailure(reason: Int) {
        if(showOnFailure) {
            sendToastIntend(onFailure)
        }
        Log.d(tag,onFailure +". Reason: $reason")
    }
    private fun sendToastIntend(message: String){
        val intent = Intent("com.lackofsky.cloud_s.SHOW_TOAST")
        intent.putExtra("message", message)
        applicationContext.sendBroadcast(intent)
    }
}