package com.lackofsky.cloud_s.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.server.NettyServer
import com.lackofsky.cloud_s.service.server.discovery.PeerDiscovery
import com.lackofsky.cloud_s.service.server.discovery.WiFiDirectManager
import com.lackofsky.cloud_s.service.server.discovery.WiFiDiscoveryByAware
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class P2PServer : Service() {
    companion object {
        const val CHANNEL_ID = "p2p_service_channel"
        const val NOTIFICATION_ID = 1
        const val SERVICE_NAME ="GrimBerry"
    }
    inner class LocalBinder : Binder() {
        fun getService(): P2PServer = this@P2PServer
    }
    @Inject lateinit var clientPartP2P: ClientPartP2P
    @Inject lateinit var notificationManager: NotificationManager

    @Inject lateinit var nettyServer: NettyServer
//    private lateinit var peerDiscovery: PeerDiscovery
    @Inject lateinit var gson: Gson

    @Inject lateinit var wifiAware: WiFiDiscoveryByAware
    @Inject lateinit var wifiDirectManager: WiFiDirectManager

  //private lateinit var protocolHandler: ProtocolHandler //TODO выбор протокола передачи данных

    private lateinit var notification: Notification
    override fun onCreate() {
        //wifiDirectManager

        Log.d("service $SERVICE_NAME", "WAS LOGGED")
        super.onCreate()
        createNotificationChannel()
//        val serviceName = Settings.Secure.getString(contentResolver, //TODO
//            Settings.Secure.ANDROID_ID)
        CoroutineScope(Dispatchers.IO).launch {
            // Запускаем сервер для входящих подключений
//            peerDiscovery.startDiscovery()
            if(wifiAware.isAwareAvailable()){
                wifiAware.startAware(SERVICE_NAME)
                Log.d("service $SERVICE_NAME", "wifiAware is started")
            }else{
                Log.d("service $SERVICE_NAME", "wifiAware is not started")
            }
            //wifiDirectManager.startPeerDiscovery()
            nettyServer.start()

        }
        Log.d("service $SERVICE_NAME", "IS STARTED")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
//        peerDiscovery.stopDiscovery()
        nettyServer.stop()
        wifiAware.stopAware()
        //wifiDirectManager.stopPeerDiscovery()
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d("service $SERVICE_NAME", "WAS ENDED")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val channel = NotificationChannel(CHANNEL_ID, SERVICE_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Описание канала"
            channel.enableLights(true)
            channel.lightColor = Color.BLUE
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300)

            // Другие настройки канала, например, важность, звуки, вибрация
            notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SERVICE_NAME")//TODO
                .setContentText("Сервис запущен")//TODO
                .setSmallIcon(R.drawable.atom_ico)
                .build()
            notificationManager.createNotificationChannel(channel)//getSystemService(NotificationManager::class.java
        }
    }
//    private fun sendWhoAmI(addr:String, port:Int){
//        val client = NettyClient(addr, port)//
//        try {
//            client.connect()
//            Log.d("service $SERVICE_NAME :client", "connected")
//            val content = gson.toJson(clientPartP2P.userOwner.value)
//            val transportData = TransportData(
//                messageType = MessageType.USER,
//                senderId = clientPartP2P.userOwner.value!!.uniqueID,
//                senderIp = "",
//                content = content
//            )
//            val json = gson.toJson(transportData)
//            client.sendMessage(json)
//            Log.d("service $SERVICE_NAME :client", "SENDED $json")
//        }catch (e: Exception){
//            Log.d("service $SERVICE_NAME :client", "catched $e")
//        }finally {
//            Log.d("service $SERVICE_NAME :client", "finally ")
//            client.close()
//        }
//    }
}