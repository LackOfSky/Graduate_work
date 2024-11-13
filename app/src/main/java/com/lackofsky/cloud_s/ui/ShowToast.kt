package com.lackofsky.cloud_s.ui

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@Composable
fun ShowToast(message: String) {
    val context = LocalContext.current
    //var showToast by remember { mutableStateOf(false) }

    // Показ Toast при изменении showToast
    //if (showToast) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            delay(2000)  // Продолжительность перед скрытием Toast
        }
            //showToast = false
        //}
    //}

    // Кнопка для отображения Toast
//    Button(onClick = { showToast = true }) {
//        Text("Показать Toast")
//    }
}