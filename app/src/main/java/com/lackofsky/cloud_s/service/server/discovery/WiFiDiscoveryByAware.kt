package com.lackofsky.cloud_s.service.server.discovery

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishConfig.Builder
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareNetworkInfo
import android.net.wifi.aware.WifiAwareNetworkSpecifier
import android.net.wifi.aware.WifiAwareSession
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

import android.util.Log
import androidx.core.content.ContextCompat
import com.lackofsky.cloud_s.service.ClientPartP2P
import dagger.hilt.android.qualifiers.ApplicationContext

class WiFiDiscoveryByAware @Inject constructor( //NOT IMPLEMENT BY HARDWARE
    @ApplicationContext private val context: Context,
    private val clientPartP2P: ClientPartP2P
) {
//    private var wifiAwareManager: WifiAwareManager? = null
    private var wifiAwareManager: WifiAwareManager? = null
    private var publishSession: PublishDiscoverySession? = null
    private var subscribeSession: SubscribeDiscoverySession? = null
    private var isAwareUp = false

    private val permissionsList = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )


    init {
        //wifiAwareManager =  context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        //
    }

    // Проверка доступности Wi-Fi Aware
    fun isAwareAvailable(): Boolean {
        Log.d("GrimBerry WiFiAware",wifiAwareManager.toString())
        return wifiAwareManager?.isAvailable ?: false
    }

    // Создание публикации
    fun startAware(serviceName: String){
        isAwareUp = true
        start(serviceName)
        sendToastIntend("Aware cluster started")
    }

    private fun start(serviceName: String) {
        checkPermission()
        wifiAwareManager =  context.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager
        val filter = IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                // discard current sessions
                //todo("уведомлять пользователя о состоянии aware\ управлять работой aware")
//            if (wifiAwareManager!!.isAvailable) {
//                if(isAwareUp){
//                }
//
//                stop()
//                //
//            } else {
//                //
//            }
            }
        }

        context.registerReceiver(myReceiver, filter)
        // #Publishing
        wifiAwareManager!!.attach(object : AttachCallback() {
            override fun onAttached(awareSession: WifiAwareSession) {
                val publishConfig = Builder()
                    .setServiceName(serviceName)
                    .build()
                checkPermission()
                awareSession.publish(publishConfig, object : DiscoverySessionCallback() {
                    override fun onPublishStarted(session: PublishDiscoverySession) {
                        publishSession = session
                        Log.d("GrimBerry WiFiAware", "Aware publishing is started")
                    }
                }, null)
            }
        }, null)
        // #Subscribing
        wifiAwareManager!!.attach( object : AttachCallback() {
            override fun onAttached(awareSession: WifiAwareSession) {
                val subscribeConfig = SubscribeConfig.Builder()
                    .setServiceName(serviceName)
                    .build()
                checkPermission() //TODO(продумать запросы в UI)

                awareSession.subscribe(subscribeConfig, object : DiscoverySessionCallback() {
                    override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                        super.onSubscribeStarted(session)
                        subscribeSession = session
                        Log.d("GrimBerry WiFiAware", "Aware subscribing is started")
                        sendToastIntend("aware is started")
                    }

                    override fun onServiceDiscovered(peerHandle: PeerHandle, serviceSpecificInfo: ByteArray, matchFilter: List<ByteArray>) {
                        connectToPeer(subscribeSession!!, peerHandle)
                        //(subscribeSession as DiscoverySession).sendMessage() //Желательно вынести сюда логику strangers
                        Log.d("GrimBerry WiFiAware", "New peer has been discovered")//TODO удалить
                        sendToastIntend("New peer has been discovered")
                    }
                }, null)
            }

        }, null)
    }



    // Завершение работы сессий
    fun stopAware() {
        isAwareUp = false
        publishSession?.close()
        subscribeSession?.close()
        //context.unregisterReceiver(myReceiver)
        Log.d("GrimBerry WiFiAware", "Aware subscribing is stopped")
        Log.d("GrimBerry WiFiAware", "Aware publishing is stopped")
        sendToastIntend("Aware cluster stopped")
    }

    private fun checkPermission():Boolean{
        val arePermissionsGranted = permissionsList.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (arePermissionsGranted) {
            return true
        }else{
            checkPermission()
        }
        return false
    }

    private fun connectToPeer(session: SubscribeDiscoverySession, peerHandle: PeerHandle) {
        val networkSpecifier = WifiAwareNetworkSpecifier.Builder(session, peerHandle)
            .setPskPassphrase("securePassword")
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d("GrimBerry WiFiAware", "Соединение установлено")
                sendToastIntend("discovered:xx")
                //startPeerToPeerCommunication(network)
            }
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
               //
                val peerAwareInfo = networkCapabilities.transportInfo as WifiAwareNetworkInfo
                clientPartP2P.sendWhoAmI(  //val socket = network.getSocketFactory().createSocket(peerIpv6, peerPort)//
                    peerAwareInfo.peerIpv6Addr!!.hostAddress!!,
                    peerAwareInfo.port
                )
                sendToastIntend("Connected to $network")
            }

            override fun onLost(network: Network) {
                Log.d("GrimBerry WiFiAware", "Соединение прервано "+ network.toString() )
                sendToastIntend("Соединение прервано")
                connectivityManager.unregisterNetworkCallback(this)
               //
            }
        })

        //TODO(бродкаст ресивер)
    }
    private fun sendToastIntend(message: String){
        val intent = Intent("com.lackofsky.cloud_s.SHOW_TOAST")
        intent.putExtra("message", message)
        context.sendBroadcast(intent)
    }
}