package com.lackofsky.cloud_s.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun ShowToast(message: String, onDismiss: (() -> Unit)? = null) {
    val context = LocalContext.current
    //var showToast by remember { mutableStateOf(false) }

    // Показ Toast при изменении showToast
    //if (showToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            delay(2000)  // Продолжительность перед скрытием Toast
            onDismiss?.invoke()
        }

}