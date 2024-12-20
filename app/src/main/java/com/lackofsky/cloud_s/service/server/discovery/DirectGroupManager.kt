package com.lackofsky.cloud_s.service.server.discovery

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DirectGroupManager (private val applicationContext: Context,
                          private val clientPartP2P: ClientPartP2P,
                    private val manager:WifiP2pManager,
                    private val channel: WifiP2pManager.Channel
) {
    private val _directGroupCreated = MutableStateFlow(false)
    val directGroupCreated: StateFlow<Boolean> = _directGroupCreated


    private val intentFilter = IntentFilter().apply {
        addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
    }

    private val wifiP2pReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            checkPermission()

            when (intent.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Log.d("DirectGroupManager GrimBerry", "Wi-Fi Direct is enabled")
                    }
                }
                WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {}
                WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                    Log.d("DirectGroupManager GrimBerry", "Wi-Fi Direct connection state changed")
                            // обработка события изменения состояния соединения
//                        val connectivityManager =
//                            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                        val networkInfo = connectivityManager.activeNetworkInfo
//
//                        if (!networkInfo!!.isConnected) {
//                            start()
//                        }
                        }
                        WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                            Log.d("WifiDirectService GrimBerry", "Device configuration changed")
                        }
                    }
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

    fun startGroup() {
        CoroutineScope(Dispatchers.Main).launch {
            checkPermission()
            applicationContext.registerReceiver(wifiP2pReceiver, intentFilter)
            manager.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    // Device is ready to accept incoming connections from peers.
                    _directGroupCreated.value = true
                    Log.d("WifiDirectService GrimBerry", "Group is created")
                    //addLocalService() to service layer
                    sendToastIntend("Server-cluster is up")

                }

                override fun onFailure(reason: Int) {
                    _directGroupCreated.value = false
                    Log.e("WifiDirectService GrimBerry", "Connection failed: $reason. service is off")

                    stopService()
                    sendToastIntend("Group creation failed: $reason. Retry to start service")
                }
            })
            delay(1000)
        }
    }
    /***   */
    fun stopGroup(){
        CoroutineScope(Dispatchers.Main).launch {
            applicationContext.unregisterReceiver(wifiP2pReceiver)
            delay(1000)
            manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    _directGroupCreated.value = false
                }

                override fun onFailure(reason: Int) {
                    sendToastIntend("Group remove failed: $reason.")
                    stopService()
                }
            })
            delay(1000)
        }
    }
    private fun stopService(){
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.stopService(intent)
    }
}

