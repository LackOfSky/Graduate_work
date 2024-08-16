package com.lackofsky.cloud_s.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lackofsky.cloud_s.Navigation
import com.lackofsky.cloud_s.data.model.ScreenRoute
import com.lackofsky.cloud_s.services.p2pService.Near
import com.lackofsky.cloud_s.ui.theme.CLOUD_sTheme
import com.lackofsky.cloud_s.ui.splash_screen.SplashScreen
import com.lackofsky.cloud_s.ui.wellcome.WelcomeScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var near: Near
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        near.start()

        enableEdgeToEdge()
        setContent {
            CLOUD_sTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                            Navigation(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        near.stop()

    }
}




