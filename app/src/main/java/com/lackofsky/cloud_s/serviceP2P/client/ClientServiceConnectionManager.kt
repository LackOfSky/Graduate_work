package com.lackofsky.cloud_s.serviceP2P.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.lackofsky.cloud_s.serviceP2P.P2PClient

class ClientServiceConnectionManager(context: Context) {
    private var serviceInterface: ClientInterface? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val localBinder = binder as P2PClient.LocalBinder
            serviceInterface = localBinder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceInterface = null
            isBound = false
        }
    }

    init {
        val intent = Intent(context, P2PClient::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun getServiceInterface(): ClientInterface = serviceInterface!!
}