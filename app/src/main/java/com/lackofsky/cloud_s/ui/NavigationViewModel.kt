package com.lackofsky.cloud_s.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity.RECEIVER_EXPORTED
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.storage.StorageRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NavigationViewModel @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository,
    private val clientPartP2P: ClientPartP2P
) : ViewModel() {

    val discoveredPeers = clientPartP2P.discoveredPeers
    private val _serviceStatus = MutableStateFlow(false)
    val serviceStatus: StateFlow<Boolean> = _serviceStatus
    private val _hostStatus = MutableStateFlow(false)
    val hostStatus: StateFlow<Boolean> = _hostStatus

    fun stopForegroundService() {
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.stopService(intent)
    }
    fun startForegroundService(action:String = "START_CLIENT") {
        val intent = Intent(applicationContext, P2PServer::class.java)
        intent.action = action
        applicationContext.startForegroundService(intent)
    }

    init{
         val serviceStatusReceiver = object : BroadcastReceiver() {
             override fun onReceive(context: Context, intent: Intent) {
                 _serviceStatus.value = intent.getBooleanExtra("status",false)
                 _hostStatus.value = intent.getBooleanExtra("isHost",false)
                 if(!_serviceStatus.value ){
                     _hostStatus.value = false
                 }
             }
         }
        val intentFilter = IntentFilter("com.lackofsky.cloud_s.SERVICE_STATUS")
        registerReceiver(applicationContext,serviceStatusReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)//TODO CHANGE TO NOT EXPORTED

    }
    fun toggleHostState(status: Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            stopForegroundService()
            delay(1000)
            if(status){
                startForegroundService("START_HOST")
            }else{
                startForegroundService()
            }
        }


//            val intent = Intent("com.lackofsky.cloud_s.c")
//            intent.putExtra("hostState", status)
//            applicationContext.sendBroadcast(intent)
    }
}