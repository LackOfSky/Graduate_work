package com.lackofsky.cloud_s.service.server.discovery

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import androidx.core.content.ContextCompat
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DirectDiscoveryManager (private val applicationContext: Context,
                              private val clientPartP2P: ClientPartP2P,
                              private val manager: WifiP2pManager,
                              private val channel: WifiP2pManager.Channel
) {
    private val TAG = "DirectDiscoveryManager GrimBerry"
    private val SERVICE_NAME = "cLoud_s"
    private val SERVICE_TYPE = "_GrimBerry._cLouds"
    private val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(
        SERVICE_NAME, SERVICE_TYPE,
        emptyMap() //mapOf("key1" to "value1", "key2" to "value2")
    )

    private val _directDiscoveryWorking = MutableStateFlow(false)
    val directDiscoveryWorking: StateFlow<Boolean> = _directDiscoveryWorking

    fun startDiscoveryService(){
        CoroutineScope(Dispatchers.Main).launch {
            checkPermission()
            manager.addLocalService(
                channel, serviceInfo, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        Log.d(TAG, "Local service added successfully")
                        _directDiscoveryWorking.value = true
                    }

                    override fun onFailure(reason: Int) {
                        Log.e(TAG, "Failed to add local service")
                        sendToastIntend("failed to add Local service. Reason: $reason")
                        stopService()
                    }
                }
            )
            delay(1000)
        }
    }
    fun stopDiscoveryService(){
        CoroutineScope(Dispatchers.Main).launch {
        checkPermission()
        manager.removeLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                _directDiscoveryWorking.value = false
                Log.d(TAG, "Local service removed successfully")
            }

            override fun onFailure(reason: Int) {
                Log.e(TAG, "Failed to remove local service. Reason: $reason")
                sendToastIntend("Failed to remove local service. Reason: $reason")
                stopService()
            }
        })
            delay(1000)
        }
    }

    /*** Discovery for searching discovery service

     */

    fun startDiscovery(){
        checkPermission()
        manager.setDnsSdResponseListeners(channel, servListener, txtListener)

        val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_TYPE)
        manager.addServiceRequest(
            channel, serviceRequest,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Service request added")
                }

                override fun onFailure(code: Int) {
                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    Log.d(TAG, "Failure to add service request. Response code: $code")
                }
            }
        )

        manager.discoverServices(channel,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d(TAG, "Discovery services started")
                }

                override fun onFailure(code: Int) {
                    // Command failed. Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    when (code) {
                        WifiP2pManager.P2P_UNSUPPORTED -> {
                            Log.d(TAG, "Wi-Fi Direct isn't supported on this device.")
                        }
                    }
                }
            }
        )

    }

    fun stopDiscovery(){

    }
    private val buddies = mutableMapOf<String, String>()

    private val servListener =
        WifiP2pManager.DnsSdServiceResponseListener { instanceName, registrationType, resourceType ->
            // Update the device name with the human-friendly version from
            // the DnsTxtRecord, assuming one arrived.
            //resourceType.deviceName = buddies[resourceType.deviceAddress] ?: resourceType.deviceName

            Log.d(TAG, "onBonjourServiceAvailable $instanceName")
        }
    private val txtListener = WifiP2pManager.DnsSdTxtRecordListener { fullDomain, record, device ->
        Log.d(TAG, "DnsSdTxtRecord available -$record")
        record["buddyname"]?.also {
            buddies[device.deviceAddress] = it
            }
        }

    fun connectToGroup(device: WifiP2pDevice){
        checkPermission()

        CoroutineScope(Dispatchers.Main).launch {
            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
                wps.setup = WpsInfo.PBC
            }
            manager.connect(channel,config,object :WifiP2pManager.ActionListener{
                override fun onSuccess() {
                    Log.d(TAG, "Discovery services started")
                }

                override fun onFailure(code: Int) {
                    // Command failed. Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    when (code) {
                        WifiP2pManager.P2P_UNSUPPORTED -> {
                            Log.d(TAG, "Wi-Fi Direct isn't supported on this device.")
                        }
                    }
                }
            })
            delay(1000)
        }
    }





    private fun stopService(){
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.stopService(intent)
    }
    private fun checkPermission():Boolean{
        val permissionsList = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.ACCESS_NETWORK_STATE
        )

        val arePermissionsGranted = permissionsList.all { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if(arePermissionsGranted){
            return true
        }
        throw Exception("DirectDiscoveryManager GrimBerry: permissions is not granted")
    }
    private fun sendToastIntend(message: String){
        val intent = Intent("com.lackofsky.cloud_s.SHOW_TOAST")
        intent.putExtra("message", message)
        applicationContext.sendBroadcast(intent)
    }

}