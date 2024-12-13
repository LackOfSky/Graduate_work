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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
    //private lateinit var peerDiscovery: PeerDiscovery
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
        CoroutineScope(Dispatchers.IO).launch {
            wifiDirectManager.start()
            nettyServer.start()

        }
        Log.d("service $SERVICE_NAME", "IS STARTED")
        sendStatusBroadcast(true)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()

        nettyServer.stop()
        wifiDirectManager.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)

        sendStatusBroadcast(false)

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
    private fun sendStatusBroadcast(status:Boolean){
        val intent = Intent("com.lackofsky.cloud_s.SERVICE_STATUS")
        intent.putExtra("status", status)
        applicationContext.sendBroadcast(intent)
    }
}