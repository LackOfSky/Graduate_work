package com.lackofsky.cloud_s.service.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.lackofsky.cloud_s.service.model.Peer
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

class PeerDiscovery(private val onPeerResolved: (Peer) -> Unit,
                    //private val onPeerDiscovered: (Peer) -> Unit,
                    private val onPeerRemoved: (Peer) -> Unit,
                    private val SERIVCE_NAME:String,
                    private val context: Context
) {
    private lateinit var jmdns: JmDNS
    private val lock: WifiManager.MulticastLock
    private val serviceType = "_http._tcp.local."
    init {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        lock = wifiManager.createMulticastLock("BerryLock")
        lock.setReferenceCounted(true)
    }
    fun startDiscovery() {
        lock.acquire()
        jmdns = JmDNS.create(getLocalhost())//

        val serviceInfo = ServiceInfo.create(serviceType, SERIVCE_NAME,setPort(),"")//todo изменять порт если он занят
        Log.d("discovery service GrimBerry", "started at " + InetAddress.getLocalHost())
        jmdns.registerService(serviceInfo)
        jmdns.addServiceListener(serviceType, object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                if(SERIVCE_NAME != event.name){
                    jmdns.requestServiceInfo(event.type, event.name)
                    Log.i("service GrimBerry added",event.type + event.name)
                }

            }

            override fun serviceRemoved(event: ServiceEvent) {
                // Обработка удаления пира
                val peer = Peer(event.name, event.info.hostAddresses.first(), event.info.port)
                Log.i("service GrimBerry removed","name: "+event.name +event.info.hostAddresses.first() +event.info.port)
                onPeerRemoved(peer)
            }

            override fun serviceResolved(event: ServiceEvent) {
                // Обработка разрешения пира
                //todo de-facto: добавить проверку условия в рабочей версии
                //if(SERIVCE_NAME != event.name) {
                    val peer = Peer(event.name, event.info.hostAddresses.first(), event.info.port)
                    Log.i(
                        "service GrimBerry resolved",
                        event.name + event.info.hostAddresses.first() + event.info.port
                    )
                    onPeerResolved(peer)

                //}

            }
        })
    }

    fun stopDiscovery() {
        jmdns.unregisterAllServices()
        jmdns.close()
        lock.release()
    }
    fun getLocalhost(): InetAddress{
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress
        val ipAddress = InetAddress.getByAddress(byteArrayOf(
            (ip and 0xff).toByte(),
            (ip shr 8 and 0xff).toByte(),
            (ip shr 16 and 0xff).toByte(),
            (ip shr 24 and 0xff).toByte()
        ))
        return ipAddress
    }
    private fun isPortInUse(port: Int): Boolean {
        return try {
            ServerSocket(port).use { false }
        } catch (e: IOException) {
            true
        }
    }
    private fun setPort():Int{
        var port = 34340
        while (isPortInUse(port)) {
            port++
        }
        return port
    }
}
