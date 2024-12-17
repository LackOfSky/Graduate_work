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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun stopForegroundService() {
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.stopService(intent)
    }
    fun startForegroundService() {
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.startForegroundService(intent)
    }

    init{
         val serviceStatusReceiver = object : BroadcastReceiver() {
             override fun onReceive(context: Context, intent: Intent) {
                 _serviceStatus.value = intent.getBooleanExtra("status",false)
             }
         }
        val intentFilter = IntentFilter("com.lackofsky.cloud_s.SERVICE_STATUS")
        registerReceiver(applicationContext,serviceStatusReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED)//TODO CHANGE TO NOT EXPORTED

    }
}