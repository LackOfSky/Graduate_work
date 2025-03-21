package com.lackofsky.cloud_s.ui.chats

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.ui.chats.components.ChatItem

sealed class ChatRoutes(val route: String) {
    object Chats : ChatRoutes("chats")
    object Chat : ChatRoutes("chat/{chatId}"){
        fun createRoute(chatId: String) = "chat/$chatId"
    }
}
@Composable
fun ChatsScreen(viewModel: ChatsViewModel = hiltViewModel(),navController: NavHostController) {
    ChatList(viewModel,navController)
}
@Composable
fun ChatList(viewModel: ChatsViewModel = hiltViewModel(), navController: NavHostController) {
    val chatList by viewModel.chats.collectAsState()
    val lastNote by viewModel.lastNoteMessage.collectAsState()
    val owner by viewModel.userOwner.collectAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        owner?.let {owner ->
            item {
                Card(//navigate
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(80.dp)
                        .padding(1.dp, 2.dp)
                        .clickable {
                            navController.navigate(ChatRoutes.Chat.createRoute(owner.uniqueID))
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color(0xfffef7ff))
                            .padding(
                                horizontal = 8.dp,
                                vertical = 8.dp
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_assignment_18),//            painter = painterResource(id = R.drawable.atom_ico),
                            contentDescription = "Image",
                            modifier = Modifier
                                .align(alignment = Alignment.Top)
                                .width(width = 70.dp)
                                .height(height = 70.dp)
                                .weight(weight = 2f)
                                .clip(shape = RoundedCornerShape(28.dp))
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                            modifier = Modifier
                                .weight(weight = 8f)
                        ) {
                                Text(
                                    text = "Notes",
                                    color = Color(0xff1d1b20),
                                    textAlign = TextAlign.Left,
                                    fontSize = 20.sp,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(12.dp, 0.dp, 4.dp, 4.dp)
//                                        .align(alignment = Alignment.CenterHorizontally)
                                )

                            Text(
                                text = lastNote?.content.orEmpty(),
                                color = Color(0xff49454f),
                                textAlign = TextAlign.Left,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .requiredWidth(150.dp)
                                    .wrapContentHeight(align = Alignment.CenterVertically)
                            )

                        }
                    }
                }
            }
        }

        chatList.let {
            items(it.toList()) { chat ->
                Card(//navigate
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(80.dp)
                        .padding(1.dp, 2.dp)
                        .clickable {
                            navController.navigate(ChatRoutes.Chat.createRoute(chat.first.chatId))
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