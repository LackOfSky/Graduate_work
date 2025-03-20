package com.lackofsky.cloud_s.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.ui.chats.ChatDialogScreen
import com.lackofsky.cloud_s.ui.chats.ChatList
import com.lackofsky.cloud_s.ui.chats.ChatRoutes
import com.lackofsky.cloud_s.ui.chats.ChatsScreen
import com.lackofsky.cloud_s.ui.friends.FriendProfile
import com.lackofsky.cloud_s.ui.friends.FriendsContainer
import com.lackofsky.cloud_s.ui.friends.FriendsScreen
import com.lackofsky.cloud_s.ui.friends.UserRoutes
import com.lackofsky.cloud_s.ui.profile.ProfileScreen
import dagger.hilt.android.qualifiers.ApplicationContext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun Navigation(modifier: Modifier = Modifier//, viewModel: ProfileViewModel = hiltViewModel()
     ){
    //val drawerState by viewModel.drawerState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

//    val profileViewModel: ProfileViewModel = hiltViewModel()
//    val friendsViewModel: FriendsViewModel = hiltViewModel()
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent { route ->
                scope.launch {
                    drawerState.close()
                }
                navController.navigate(route)
            }
        },
        content = {
            // Main content
            Column(modifier = Modifier) {
                TopBar(drawerState = drawerState,
                        scope = scope)
                NavHost(navController, startDestination = "profile") {
                    composable("profile") { ProfileScreen() }
                    composable("friends") { FriendsScreen( navController = navController)}//  FriendsScreen(navController = navController) }
                    composable("messages") { ChatsScreen(navController = navController) }//TODO (исправить дубль)
//                    composable(ChatRoutes.Chats.route) { ChatList(navController = navController) }//
                    composable(
                        route = ChatRoutes.Chat.route,
                        arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val chatId = backStackEntry.arguments?.getString("chatId") ?: "messages"
                        //ChatList(viewModel = viewModel, navController = navController)
                        ChatDialogScreen(chatId = chatId)
                    }
//                    composable(UserRoutes.Users.route) { FriendsContainer( navController = navController) }
                    composable(
                        route = UserRoutes.User.route,
                        arguments = listOf(navArgument("userId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getInt("userId") ?: -1
                        FriendProfile(userId = userId)
                    }
                    composable("Testing") { TestingDirect() }
                    composable("HostState") { TestingDirect() }

//                    composable("qr") { QRScreen() }
                }
            }
        })
}

@Composable
fun TopBar(modifier: Modifier = Modifier,drawerState: DrawerState,scope:CoroutineScope){
    Row(//
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 48.dp, 8.dp, 8.dp),//TODO OOOO
        // Make the Row occupy full width
        horizontalArrangement = Arrangement.SpaceBetween // Distribute space between composables
    ) {
        Button(
            onClick = {
                scope.launch {
                    drawerState.open()
                }
            }, colors = ButtonDefaults.buttonColors(
                contentColor = Color(0xff004D40),       // цвет текста
                containerColor = Color(0xffffffff)
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(12.dp),
            contentPadding = PaddingValues(
                horizontal = 16.dp, // Set horizontal padding to 20dp
                vertical = 5.dp // Set vertical padding to 10dp
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_menu_36),
                contentDescription = "Icon",
                modifier = Modifier
                    .height(height = 36.dp)//,
                //tint = Color(0xff1d1b20)
            )
        }
        Card(
            elevation = CardDefaults.cardElevation(1.dp),
            colors = CardDefaults.cardColors(Color.White),
        ) {
            Text(text = "Main page",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(10.dp,10.dp,60.dp,10.dp)
            )
        }

        //settings
        Button(
            onClick = {
//                scope.launch {
//                    drawerState.open()
//                }
            }, colors = ButtonDefaults.buttonColors(
                contentColor = Color(0xff004D40),       // цвет текста
                containerColor = Color(0xffffffff)
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(12.dp),
            contentPadding = PaddingValues(
                horizontal = 16.dp, // Set horizontal padding to 20dp
                vertical = 5.dp // Set vertical padding to 10dp
            ),
            modifier = Modifier

        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_more_vert_36),
                contentDescription = "Icon",
                modifier = Modifier
                    .height(height = 36.dp)//,
                //tint = Color(0xff1d1b20)
            )
        }
    }//
}

@Composable //Menu
fun DrawerContent(onMenuItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Menu",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(vertical = 11.dp)
                .alpha(0.5f))
        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Button(onClick = { onMenuItemClick("Profile") }) {
            Text(text = "Profile")
        }
        Button(onClick = { onMenuItemClick("Friends") }) {
            Text(text = "Friends")
        }
        Button(onClick = { onMenuItemClick("Messages") }) {
            Text(text = "Messages")
        }
        Button(onClick = { onMenuItemClick("QR") }) {
            Text(text = "QR")
        }
        Button(onClick = { onMenuItemClick("Testing") }) {
            Text(text = "testing wifi-direct direct")
        }
        Divider(Modifier.width(100.dp))
        MySwitch()
        HostSwitch()
    }
}

@Composable
fun MySwitch( viewModel: NavigationViewModel = hiltViewModel()) {
    var isChecked = viewModel.serviceStatus.collectAsState()
    Column(modifier = Modifier.padding(5.dp)){
//        Card(Modifier.fillMaxWidth().height(40.dp)){
//            Text(text = "Status: ",
//                style = MaterialTheme.typography.titleLarge,
//                color = Color.Black,
//                modifier = Modifier.padding(5.dp)
//            )
//        }
        Button(onClick = { /*TODO*/ },Modifier.height(50.dp)) {


        Text(text = if(isChecked.value) "Online " else "Offline ",
        )
        Switch(
            checked = isChecked.value,
            onCheckedChange = { //isChecked = it
                if(isChecked.value){
                    viewModel.stopForegroundService()
                }else{
                    viewModel.startForegroundService()
                }
               },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xff80ff80),
                uncheckedThumbColor = Color.Gray
            ), modifier = Modifier.padding(5.dp)
        )
    }
    }

//
}
@Composable
fun HostSwitch( viewModel: NavigationViewModel = hiltViewModel()) {
    val isChecked = viewModel.hostStatus.collectAsState()
    val isEnabled = viewModel.serviceStatus.collectAsState()
    Column(modifier = Modifier.padding(5.dp)){
        Button(onClick = { /*TODO*/ },Modifier.height(50.dp)) {
            Text(text = if(isChecked.value) "as host " else "as client ",
            )
            Switch(
                checked = isChecked.value,
                enabled = isEnabled.value,
                onCheckedChange = { //isChecked = it
                    if(isChecked.value){
                        viewModel.toggleHostState(false)
                    }else{
                        viewModel.toggleHostState(true)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xff80ff80),
                    uncheckedThumbColor = Color.Gray
                ), modifier = Modifier.padding(5.dp)
            )
        }
    }

//
}

@Composable //TODO
fun DrawerSettingsContent(onMenuItemClick: (String) -> Unit) {
    Box(modifier = Modifier){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .align(Alignment.TopEnd)
    ) {
        Text(text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(vertical = 11.dp)
                .alpha(0.5f))
        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Button(onClick = { onMenuItemClick("Item 5") }) {
            Text(text = "Item 5")
        }
        Button(onClick = { onMenuItemClick("Item 6") }) {
            Text(text = "Item 6")
        }
        Button(onClick = { onMenuItemClick("Item 4") }) {
            Text(text = "Item 4")
        }
    }
    }
}

@Composable
fun TestingDirect(viewModel: NavigationViewModel = hiltViewModel()){
    val peers by viewModel.discoveredPeers.collectAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        items(peers.toList()) { peer ->
            Card(//navigate
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(80.dp)
                    .padding(1.dp, 2.dp)
                    .clickable {
                        //navController.navigate(UserRoutes.User.createRoute(peer.first.id))
                    }
            ) {
                Text(peer.address)
                Text(peer.name)
                //FriendItem(peer.user)

            }
        }
        item{Text(text="",modifier = Modifier.height(80.dp))}
    }
}