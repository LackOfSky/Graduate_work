package com.lackofsky.cloud_s.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
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
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import com.lackofsky.cloud_s.service.server.NettyServer
import com.lackofsky.cloud_s.service.server.discovery.PeerDiscovery
import com.lackofsky.cloud_s.service.server.discovery.WiFiDirectManager
import com.lackofsky.cloud_s.service.server.discovery.WiFiDirectService
import com.lackofsky.cloud_s.service.server.discovery.WiFiDiscoveryByAware
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val TAG = "GrimBerry P2P (P2PServer)"
    @Inject lateinit var clientPartP2P: ClientPartP2P
    @Inject lateinit var notificationManager: NotificationManager

    @Inject lateinit var nettyServer: NettyServer
    //private lateinit var peerDiscovery: PeerDiscovery
    @Inject lateinit var gson: Gson

    //@Inject lateinit var wifiAware: WiFiDiscoveryByAware
    //@Inject lateinit var wifiDirectManager: WiFiDirectManager
    @Inject lateinit var wiFiDirectService: WiFiDirectService
  //private lateinit var protocolHandler: ProtocolHandler //TODO выбор протокола передачи данных

    private val _serviceState = MutableStateFlow(P2pServiceState.STOPPED)



    private lateinit var notification: Notification
    override fun onCreate() {
        super.onCreate()
    }
    /***
     * - остановка составляющих сервиса
     * - остановка менеджеров wifi-direct
     * - остановка nsd и netty*/
    override fun onDestroy() {
        super.onDestroy()
        stopService()
        //unregisterReceiver(serviceHostStateReceiver)
        Log.w(TAG+" MAIN", "MAIN SERVICE HAS BEEN STOPPED")
        stopForeground(STOP_FOREGROUND_REMOVE)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.w(TAG+" MAIN", "MAIN SERVICE HAS BEEN STARTED")
        when (intent?.action) {
            "START_HOST" -> {
                startHost()
            }
            "START_CLIENT" -> {
                startClient()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun startHost(){
        //CoroutineScope(Dispatchers.Main).launch {
            when(_serviceState.value){
                P2pServiceState.STOPPED -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        wiFiDirectService.startDiscoveryService()
                        val port = nettyServer.start()
                        Log.i(TAG,"netty server has been started at port: $port.")
                        wiFiDirectService.startNSD(port)
                    }

                }
                P2pServiceState.WORKING_CLIENT -> {
                    stopService()
                    startHost()
                }
                P2pServiceState.WORKING_HOST -> {
                    Log.e(TAG,"attempt start host while host is working")
                }
            }
        _serviceState.value = P2pServiceState.WORKING_HOST
            sendStatusBroadcast(true)

            Log.i(TAG,"Service has been started as host")
        //}
        createNotificationChannel()
    }
    private fun startClient(){

        //CoroutineScope(Dispatchers.Main).launch {
            when(_serviceState.value){
                P2pServiceState.WORKING_HOST->{
                    stopService()
                    startClient()
                }
                P2pServiceState.WORKING_CLIENT->{
                    Log.e(TAG,"attempt start client while client is working")
                }
                P2pServiceState.STOPPED->{
                    CoroutineScope(Dispatchers.IO).launch{
                        val port = nettyServer.start()
                        wiFiDirectService.startNSD(port)
                    }


                }
            }
            _serviceState.value = P2pServiceState.WORKING_CLIENT
            sendStatusBroadcast(true)
            //delay(1000)
            Log.i(TAG,"service was started as client")
        createNotificationChannel()
    }
    private fun stopService(){
        sendStatusBroadcast(false)
        CoroutineScope(Dispatchers.Main).launch {
            when(_serviceState.value){
                P2pServiceState.STOPPED -> {
                    Log.e(TAG,"attempt to stop stopped service")
                }
                P2pServiceState.WORKING_CLIENT -> {
                }
                P2pServiceState.WORKING_HOST -> {
                    wiFiDirectService.stopDiscoveryService()
                }
            }
            wiFiDirectService.stopNSD()
            nettyServer.stop()
            _serviceState.value = P2pServiceState.STOPPED
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            //delay(1000)
            Log.i(TAG,"Service was stopped")
        }
    }

    /***inner function for display online-status of service*/
    private fun sendStatusBroadcast(status:Boolean){
        val intent = Intent("com.lackofsky.cloud_s.SERVICE_STATUS")
        intent.putExtra("status", status)
        if(_serviceState.value ==P2pServiceState.WORKING_HOST){
            intent.putExtra("isHost",true)
        }else{
            intent.putExtra("isHost",false)
        }
        applicationContext.sendBroadcast(intent)
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
}

enum class P2pServiceState{
    STOPPED, WORKING_CLIENT,WORKING_HOST
}