package com.lackofsky.cloud_s.service.server.discovery

import android.content.Context
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer
import com.lackofsky.cloud_s.service.server.NettyServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.InetAddress
import javax.inject.Inject

class NSDManager (private val applicationContext: Context,
                  private val clientPartP2P: ClientPartP2P
) {
    private val TAG = "GrimBerry NSDManager"
    private val SERVICE_NAME = "cLoud_s"
    private val SERVICE_TYPE = "_cLouds._tcp."
    private var SERVICE_PORT: Int? = null
    private var nsdManager: NsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager


    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            //mServiceName = NsdServiceInfo.serviceName
            Log.d(TAG, " service registered successfully. $serviceInfo")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.d(TAG, " service registration failed. Reason $errorCode")
            // Registration failed! Put debugging code here to determine why.
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG, " service is unregistered")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            Log.d(TAG, " service  unregistration failed. Reason $errorCode")
        }
    }

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolved Successfully. $serviceInfo")
//            if (serviceInfo.serviceName == SERVICE_NAME) {
//                Log.d(TAG, "Same IP.")
//                return
//            }
            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host

            clientPartP2P.sendWhoAmI(host.hostAddress!!,port, ownPort = SERVICE_PORT!!)
            //
        }
    }
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success. $service")
            when {
                service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
                service.serviceName == SERVICE_NAME -> // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000)
                        nsdManager.resolveService(service, resolveListener)
                    }

                    Log.d(TAG, "Same machine: $SERVICE_NAME")
                }

                service.serviceName.contains(SERVICE_NAME) -> nsdManager.resolveService(service, resolveListener)
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.i(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            sendToastIntend("Start discovery failed: Error code:$errorCode")
            //nsdManager.stopServiceDiscovery(this)
            stopService()
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            sendToastIntend("Stop Discovery failed: Error code:$errorCode")
            //nsdManager.stopServiceDiscovery(this)
            stopService()
        }
    }

    fun startNSDService(port:Int = 0) {
        //nsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager   transfered
        Log.d("GrimBerry","NSD  set port $port")
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            SERVICE_PORT = port
            setPort(SERVICE_PORT!!)
        }
        nsdManager.apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
            discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        }
        Log.d("GrimBerry","NSD service Info port: ${serviceInfo.port}")
        Log.d(TAG,"NSD has been started")
        Log.i(TAG,"NSD service name: $SERVICE_NAME, service type: $SERVICE_TYPE")

    }
    fun stopNSDService(){
//        registrationListener.let {
//            nsdManager.unregisterService(it)
//        }

        nsdManager.apply {
            unregisterService(registrationListener)
            stopServiceDiscovery(discoveryListener)

        }
        Log.d(TAG,"NSD has been stopped")
    }

    private fun stopService(){
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.stopService(intent)
    }
    private fun sendToastIntend(message: String){
        val intent = Intent("com.lackofsky.cloud_s.SHOW_TOAST")
        intent.putExtra("message", message)
        applicationContext.sendBroadcast(intent)
    }
}