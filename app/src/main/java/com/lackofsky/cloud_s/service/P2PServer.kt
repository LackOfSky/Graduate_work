package com.lackofsky.cloud_s.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.data.SharedState
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.server.NettyServer
import com.lackofsky.cloud_s.service.server.PeerDiscovery
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class P2PServer : Service() {
    companion object {
        const val CHANNEL_ID = "p2p_service_channel"
        const val NOTIFICATION_ID = 1
        const val SERVICE_NAME ="GrimBerry"
    }
    @Inject lateinit var sharedState: SharedState
    @Inject lateinit var notificationManager: NotificationManager

    @Inject lateinit var nettyServer: NettyServer
    private lateinit var peerDiscovery: PeerDiscovery
    private val gson = Gson()
  //private lateinit var protocolHandler: ProtocolHandler //TODO выбор протокола передачи данных
//    nettyClient.sendMessage(
//    gson.toJson(transportData)
//    )
    private lateinit var notification:Notification
    override fun onCreate() {
        Log.d("service $SERVICE_NAME", "WAS LOGGED")
        super.onCreate()
        createNotificationChannel()
        val serviceName = Settings.Secure.getString(contentResolver,
            Settings.Secure.ANDROID_ID)
        // Запускаем поиск пиров
        peerDiscovery = PeerDiscovery(
            onPeerResolved = { discoveredPeer ->
                CoroutineScope(Dispatchers.IO).launch{
                    sendWhoAmI(discoveredPeer.address, discoveredPeer.port)
                }

                },
            onPeerRemoved = {removePeer ->
                sharedState.removeActiveUser(removePeer)
                },
                serviceName,
                nettyServer.getDefaultPort(),
                applicationContext
//            onPeerResolved = {resolvedPeer ->
//                peers.remove(removePeer)
//                updatePeersLiveData()}
        )
        CoroutineScope(Dispatchers.IO).launch {
            // Запускаем сервер для входящих подключений
            peerDiscovery.startDiscovery()
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
        peerDiscovery.stopDiscovery()
        nettyServer.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d("service $SERVICE_NAME", "WAS ENDED")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
    private fun sendWhoAmI(addr:String, port:Int){
        val client = NettyClient(sharedState,addr, port)//
        try {
            client.connect()
            Log.d("service $SERVICE_NAME :client", "connected")
            val content = gson.toJson(sharedState.userOwner.value)
            val transportData = TransportData(
                messageType = MessageType.USER,
                senderId = sharedState.userOwner.value!!.uniqueID,
                senderIp = "",
                content = content
            )
            val json = gson.toJson(transportData)
            client.sendMessage(json)
            Log.d("service $SERVICE_NAME :client", "SENDED $json")
        }catch (e: Exception){
            Log.d("service $SERVICE_NAME :client", "catched $e")
        }finally {
            Log.d("service $SERVICE_NAME :client", "finally ")
            client.close()
        }
    }
}