package com.lackofsky.cloud_s.service.server.discovery

import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.server.NettyServer
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class WiFiDirectService @Inject constructor(private val applicationContext: Context,
                                            private val clientPartP2P: ClientPartP2P) {
    private val TAG = "GrimBerry WiFiDirectService"
    private val manager: WifiP2pManager by lazy {
        applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }  //вынести в хилт
    private val channel: WifiP2pManager.Channel by lazy {
        manager.initialize(applicationContext, applicationContext.mainLooper, null)
    } //вынести в хилт

    private val directGroupManager =  DirectGroupManager(applicationContext,clientPartP2P,manager,channel)
    private val directDiscoveryManager = DirectDiscoveryManager(applicationContext,clientPartP2P,manager,channel)
    private val nsdManager = NSDManager(applicationContext,clientPartP2P)

    /*** на данном этапе сервис будет останавливатся при неудачном вызове методов startGroup, startDiscoveryService*/
    fun startDiscoveryService(){
        directGroupManager.startGroup()
        directDiscoveryManager.startDiscoveryService()
    }
    fun stopDiscoveryService(){
        directGroupManager.stopGroup()
        directDiscoveryManager.stopDiscoveryService()
    }

    /*** function for controlling nsd + netty server */
    fun startNSD(port: Int){
        //nettyServer = server
        //val port = nettyServer.start()

        nsdManager.startNSDService(port)
        Log.i(TAG,"NSD service has been started.")
    }
    /*** function for controlling nsd + netty server */
    fun stopNSD(){
        nsdManager.stopNSDService()
        Log.i(TAG,"netty server has been stopped")
        //nettyServer.stop()
        Log.i(TAG,"NSD service has been stopped.")
    }
}