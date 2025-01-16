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
import java.util.concurrent.Executors
import javax.inject.Inject

class NSDManager (private val applicationContext: Context,
                  private val clientPartP2P: ClientPartP2P
) {
    private val TAG = "GrimBerry NSDManager"
    private var SERVICE_NAME = "cLoud_s"
    private val SERVICE_TYPE = "_cLouds._tcp."
    private var SERVICE_PORT: Int? = null
    private var nsdManager: NsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val registeredServices = mutableSetOf<String>()
    private var serviceInfo: NsdServiceInfo? = null

    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")
            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host
            clientPartP2P.sendWhoAmI(host.hostAddress!!, port, ownPort = SERVICE_PORT!!)
        }
    }

//    val serviceInfoCallback = object : NsdManager.ServiceInfoCallback {
//        override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
//            Log.e(TAG, "NSDManager. Service info callback registration failed. code: $errorCode")
//        }
//        override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
//            // Обработка обновлённой информации о сервисе
//            Log.i(TAG, "NSDManager. Resolved Successfully. $serviceInfo")
////            if (serviceInfo.serviceName == SERVICE_NAME) {//TURN IT OFF for testing
////                Log.d(TAG, "NSDManager. Same IP.")
////                return
////            }
//
////            val host = serviceInfo.host
////            val port = serviceInfo.port
////            clientPartP2P.sendWhoAmI(host.hostAddress!!, port, ownPort = SERVICE_PORT!!)
//        }
//
//        override fun onServiceLost() {
//            Log.d(TAG, "NSDManager. Service lost.")
//        }
//
//        override fun onServiceInfoCallbackUnregistered() {
//            Log.d(TAG, "NSDManager. Service info callback unregistered")
//        }
//
//    }
    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            //mServiceName = NsdServiceInfo.serviceName
            //TODO( CHECK IT)
            Log.e(TAG, "NSDManager. service registered successfully. $serviceInfo")
            Log.e(TAG, "NSDManager. Preview service name $SERVICE_NAME")
            SERVICE_NAME = serviceInfo.serviceName
            Log.e(TAG, "NSDManager. New service name $SERVICE_NAME")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            Log.d(TAG, "NSDManager. service registration failed. Reason $errorCode")
            // Registration failed! Put debugging code here to determine why.
        }

        override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d(TAG, "NSDManager. service is unregistered")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            Log.d(TAG, "NSDManager. Service unregistration failed. Reason $errorCode")
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "NSDManager. Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(TAG, "NSDManager. Service discovery success. $service")
            when {
                service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "NSDManager. Unknown Service Type: ${service.serviceType}")
//                service.serviceName ==  serviceInfo!!.serviceName ->{
//                    Log.d(TAG, "NSDManager. Same machine: $SERVICE_NAME")
//                    return
//                }
                service.serviceName.contains(SERVICE_NAME) ->{nsdManager.resolveService(service, resolveListener)
//                    nsdManager.registerServiceInfoCallback(
//                        service,
//                        Executors.newSingleThreadExecutor(),
//                        serviceInfoCallback)
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.i(TAG, "NSDManager. service lost: $service")
            //nsdManager.unregisterServiceInfoCallback(serviceInfoCallback)
            //TODO(рассмотреть вариант удаления соответствующего пользователя)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "NSDManager. Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "NSDManager. Discovery failed: Error code:$errorCode")
            sendToastIntend("Start discovery failed: Error code:$errorCode")
            stopService()
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "NSDManager. Discovery failed: Error code:$errorCode")
            sendToastIntend("Stop Discovery failed: Error code:$errorCode")
            //nsdManager.stopServiceDiscovery(this)
            stopService()
        }
    }

    fun startNSDService(port:Int = 0) {
        //nsdManager = applicationContext.getSystemService(Context.NSD_SERVICE) as NsdManager   transfered
        Log.d("GrimBerry","NSD  set port $port")
        serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = SERVICE_NAME
            serviceType = SERVICE_TYPE
            SERVICE_PORT = port
            setPort(SERVICE_PORT!!)
        }
        nsdManager.apply {
            discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
        Log.i(TAG,"NSD has been started. Port: $SERVICE_PORT")

    }
    fun stopNSDService(){
//        registrationListener.let {
//            nsdManager.unregisterService(it)
//        }

        nsdManager.apply {
            unregisterService(registrationListener)
            stopServiceDiscovery(discoveryListener)

        }
        Log.d(TAG,"NSDManager. NSD has been stopped")
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