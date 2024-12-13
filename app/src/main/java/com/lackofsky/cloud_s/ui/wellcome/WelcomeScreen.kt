package com.lackofsky.cloud_s.ui.wellcome

import android.annotation.SuppressLint
import android.system.Os
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@SuppressLint("HardwareIds")
@Composable
fun WelcomeScreen(screenController: NavHostController, viewModel: WelcomeViewModel = hiltViewModel()){
    var fullName by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var info by remember { mutableStateOf("") }
    val context = LocalContext.current
    val uniqueID = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("login") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = about,
            onValueChange = { about = it },
            label = { Text("about") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = info,
            onValueChange = { info = it },
            label = { Text("info") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Button(onClick = {
            viewModel.saveUser(
                User(id = 1,fullName = fullName, login = login, uniqueID = uniqueID),
                UserInfo(userId = uniqueID, about = about, info = info)
            )
            screenController.navigate("main")
        }) {
            Text("Save")
        }
    }
}
