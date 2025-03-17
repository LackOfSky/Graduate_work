package com.lackofsky.cloud_s.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lackofsky.cloud_s.data.model.ScreenRoute
import com.lackofsky.cloud_s.service.P2PServer
import com.lackofsky.cloud_s.ui.splash_screen.SplashScreen
import com.lackofsky.cloud_s.ui.theme.CLOUD_sTheme
import com.lackofsky.cloud_s.ui.wellcome.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalLayoutApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private lateinit var p2PServer: Intent
    // List of permissions to request
    private val permissionsList = listOf(
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.INTERNET,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.NEARBY_WIFI_DEVICES
    )
    private val permissionList34 = listOf(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
    private var broadcastReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        near.start()
//        p2PServer = Intent(applicationContext, P2PServer::class.java)
        WindowCompat.
            setDecorFitsSystemWindows(window, false)
            broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val message = intent?.getStringExtra("message")
                ?: "Ошибка"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

// Регистрация BroadcastReceiver в onCreate() Activity
        val intentFilter = IntentFilter("com.lackofsky.cloud_s.SHOW_TOAST")
        registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED)

        enableEdgeToEdge()
        setContent {
            CLOUD_sTheme {
                var allPermissionsGranted by remember { mutableStateOf(false) }
                // Permissions launcher
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    allPermissionsGranted = permissions.values.all { it }
                }
                // Check if permissions are already granted
                    val arePermissionsGranted = permissionsList.all { permission ->
                        ContextCompat.checkSelfPermission(
                            LocalContext.current,
                            permission
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    }


//                val arePermissionsGranted = permissionsList.all { permission ->
//                    ContextCompat.checkSelfPermission(
//                        LocalContext.current,
//                        permission
//                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
//                }
                if (arePermissionsGranted) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val arePermissions34Granted = permissionList34.all { permission ->
                            ContextCompat.checkSelfPermission(
                                LocalContext.current,
                                permission
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        }
                        allPermissionsGranted = arePermissions34Granted
                    }else {
                        allPermissionsGranted = true
                    }
                }
                // Show different UI based on permission status
                if (allPermissionsGranted) {
                    //startForegroundService(p2PServer)
                    // Показать другую компоненту, если все разрешения предоставлены
                    Scaffold(modifier = Modifier.fillMaxSize().padding(WindowInsets.ime.asPaddingValues())
                    ) { innerPadding ->
                        //todo ЕСЛИ в бд нету пользователя - страница велком, если есть - дальше
                        val screenController = rememberNavController()
                        NavHost(screenController, startDestination = ScreenRoute.SPLASH.route) {
                            composable(ScreenRoute.SPLASH.route){
                                SplashScreen(navController = screenController)
                            }
                            composable(ScreenRoute.WELCOME.route) {
                                WelcomeScreen(screenController)
                            }
                            composable(ScreenRoute.MAIN.route) {
                                Navigation(modifier = Modifier.padding(innerPadding))//WindowInsets.ime.asPaddingValues()
                            }
                        }
                    }
                } else {
                    // UI для запроса разрешений
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(text = "Permissions are required to proceed")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { permissionLauncher.launch(permissionsList.toTypedArray()) }) {
                                Text(text = "Request Permissions")
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        val intent = Intent(applicationContext, P2PServer::class.java)
        applicationContext.stopService(intent)
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)

    }
}




