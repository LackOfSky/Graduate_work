package com.lackofsky.cloud_s.service.server.discovery

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.server.NettyServer
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class WiFiDirectService @Inject constructor(private val directGroupManager: DirectGroupManager,
                                            private val directDiscoveryManager: DirectDiscoveryManager,
                                            private val nsdManager: NSDManager
    ) {
    private val TAG = "GrimBerry WiFiDirectService"

    init{
        Log.i(TAG,"NSD service is up.")
        //startNSD(228)
    }
    /*** на данном этапе сервис будет останавливатся при неудачном вызове методов startGroup, startDiscoveryService*/
    fun startDiscoveryService(){
        directGroupManager.startGroup()
        directDiscoveryManager.startDiscoveryService()
    }
    fun stopDiscoveryService(){
        directGroupManager.stopGroup()
        directDiscoveryManager.stopDiscoveryService()
    }

    fun startNSD(port: Int){
        nsdManager.startNSDService(port)
        Log.i(TAG,"NSD service has been started.")
    }

    fun stopNSD(){
        nsdManager.stopNSDService()
        Log.i(TAG,"NSD service has been stopped.")
    }
}