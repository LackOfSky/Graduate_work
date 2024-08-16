package com.lackofsky.cloud_s.ui.splash_screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.lackofsky.cloud_s.data.model.ScreenRoute
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController, viewModel: SplashScreenViewModel = hiltViewModel()) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        delay(200)
        viewModel.checkIsUserLoggedIn()
        when(isUserLoggedIn) {
            true -> {
                navController.navigate(ScreenRoute.MAIN.route) {
                    popUpTo(ScreenRoute.SPLASH.route) { inclusive = true }
                }
            }
            false -> {
                navController.navigate(ScreenRoute.WELCOME.route) {
                    popUpTo(ScreenRoute.SPLASH.route) { inclusive = true }
                }
            }
            null -> { }
        }
    }


    // Показываем какой-нибудь сплэш-экран (анимация или логотип)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...")
    }
}