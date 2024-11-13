package com.lackofsky.cloud_s.ui.chats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.tooling.preview.Preview
import com.lackofsky.cloud_s.ui.chats.components.ChatItem

sealed class ChatRoutes(val route: String) {
    object Chats : ChatRoutes("chats")
    object Chat : ChatRoutes("chat/{chatId}"){
        fun createRoute(chatId: String) = "chat/$chatId"
    }
}
@Composable
fun ChatsScreen(viewModel: ChatsViewModel = hiltViewModel(),navController: NavHostController) {
    //var isPeerRequested by remember { mutableStateOf(false) }
//    val friendPlaceholder by viewModel.currentUser.collectAsState()//TODO friends placeholder
//    val strangers by viewModel.strangers.collectAsState()

    //val tabIndex = remember { mutableStateOf(0) }
//    FriendsTabs(viewModel = viewModel,
//        navController = navController)
    ChatList(viewModel,navController)
}
@Composable
fun ChatList(viewModel: ChatsViewModel = hiltViewModel(), navController: NavHostController) {
    val chatList by viewModel.chats.observeAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        chatList?.let {
            item{  Text("20303030")}
            items(it.toList()) { chat ->
                Card(//navigate
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(80.dp)
                        .padding(1.dp, 2.dp)
                        .clickable {
                            navController.navigate(ChatRoutes.Chat.createRoute(chat.chatId))
                        }
                ) {
                    ChatItem(viewModel,chat)
                }
            }
        }
        item{ Text(text="",modifier = Modifier.height(80.dp)) }
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun ExampleChatScreen() {
    val navController = rememberNavController()
    ChatsScreen(navController = navController)
}