package com.lackofsky.cloud_s.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.lackofsky.cloud_s.ui.friends.components.FriendItem
import com.lackofsky.cloud_s.ui.ShowToast
import com.lackofsky.cloud_s.ui.friends.components.PendingStrangerItem
import com.lackofsky.cloud_s.ui.friends.components.StrangerItem



sealed class UserRoutes(val route: String) {
    object Users : UserRoutes("users")
    object User : UserRoutes("user/{userId}"){
        fun createRoute(userId: Int) = "user/$userId"
    }
}
@Composable
fun FriendsScreen(viewModel: FriendsViewModel = hiltViewModel(),
                  navController: NavHostController) {
    //var isPeerRequested by remember { mutableStateOf(false) }
    FriendsContainer(viewModel = viewModel,
        navController = navController)
}


@Composable
fun FriendsContainer(viewModel: FriendsViewModel = hiltViewModel(),
                     navController: NavHostController){
    val pending by viewModel.pendingStrangers.collectAsState(emptyList())
    val tabIndex = remember { mutableStateOf(0) }
    Column {
        // Компонент вкладок
        key(pending.size) {
            TabRow(selectedTabIndex = tabIndex.value) {
                viewModel.tabTitlesList.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex.value == index,
                        onClick = {
                            tabIndex.value = index
                        },
                        text = { Text(text = title) }
                    )
                }
                Tab(
                    selected = tabIndex.value == 2,
                    onClick = {
                        if (pending.isNotEmpty()) {
                            tabIndex.value = 2
                        }
                    },
                    enabled = pending.isNotEmpty(),
                    text = {
                        if (pending.isNotEmpty()) Text(text = viewModel.tabTitlesItem) else Text(
                            text = viewModel.tabTitlesItem,
                            color = Color.Gray
                        )
                    }
                )
                if(tabIndex.value == 2 && pending.isEmpty()) tabIndex.value = 0
                if (pending.isNotEmpty()) {
                    ShowToast(message = "Incoming request")
                }
            }
        }
        // Содержимое вкладок
        when (tabIndex.value) {
            0 -> FriendList(viewModel,navController)
            1 -> PeerList(viewModel,navController)//AddFriends()
            2 -> PendingList(viewModel,navController)
        }
    }
}

@Composable
fun FriendList(viewModel: FriendsViewModel,navController: NavHostController) {
    val friendsOnline by viewModel.friendsOnline.collectAsState(null)
    val friendsOffline by viewModel.friendsOffline.collectAsState(null)
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        item{Text(text="Online",modifier = Modifier.height(40.dp))}
        friendsOnline?.let {
            items(it.toList()) { friend ->
                Card(//navigate
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(80.dp)
                        .padding(1.dp, 2.dp)
                        .clickable {
                            navController.navigate(UserRoutes.User.createRoute(friend.id))
                        }
                ) {
                    FriendItem(friend, navController  = navController, isOnline = true)
                }
            }
        }
        if(friendsOnline.isNullOrEmpty()) item { Text(text = "Now list is empty.",
            modifier = Modifier.padding(16.dp)
        )}
        item { Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), thickness = 1.dp, color = Color.LightGray ) }
        item{Text(text="All friends",modifier = Modifier.height(40.dp))}

        friendsOffline?.let {
            items(it.toList()) { friend ->
                Card(//navigate
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(80.dp)
                        .padding(1.dp, 2.dp)
                        .clickable {
                            navController.navigate(UserRoutes.User.createRoute(friend.id))
                        }
                ) {
                    FriendItem(friend, navController  = navController, isOnline = false)
                }
            }
        }
        if(friendsOffline.isNullOrEmpty()) item { Text(text = "Now list is empty.",
            modifier = Modifier.padding(16.dp)
        )}
        item{Text(text="",modifier = Modifier.height(80.dp))}

    }


}

@Composable
fun PeerList(viewModel: FriendsViewModel, navController: NavHostController) {
    val peers by viewModel.peers.collectAsState(emptySet())

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
                        navController.navigate(UserRoutes.User.createRoute(peer.id))
                    }
            ) {
                //FriendItem(peer.user)
                    StrangerItem(peer)

            }
        }
        if (peers.isEmpty()) {
            item {
                Text(
                    text = "Now list is empty.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        item{Text(text="",modifier = Modifier.height(80.dp))}
        //item{AddFriends()}
    }
}

//@Composable
//fun AddFriends() {
//    Column(modifier = Modifier
//        .fillMaxSize()
//        .padding(16.dp)) {
//        Text(text = "Enter a login",
//                    fontSize = 24.sp,
//            style = MaterialTheme.typography.headlineSmall,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth()
//        )
//        val login = remember{mutableStateOf("")}
//        TextField(
//            login.value,
//            {login.value = it},
//            placeholder = { Text("Enter a name of your friend..") },
//            modifier = Modifier.fillMaxWidth()
//        )
//        Text(text = "or",
//            fontSize = 22.sp,
//            style = MaterialTheme.typography.bodyLarge,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.fillMaxWidth()
//        )
//        Card {
//            TextButton(onClick = { /*TODO*/ }) {
//                Text(text = "Scan QR-code",
//                    fontSize = 24.sp,
//                    style = MaterialTheme.typography.headlineSmall,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//        }
//    }
//}

@Composable
fun PendingList(viewModel: FriendsViewModel,navController:NavHostController) {
    val pendingList by viewModel.pendingStrangers.collectAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        items(pendingList.toList()) { peer ->
            Card(//navigate
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(80.dp)
                    .padding(1.dp, 2.dp)
                    .clickable {
                        navController.navigate(UserRoutes.User.createRoute(peer.id))
                    }
            ) {
                PendingStrangerItem(peer, viewModel)
            }
        }
        item{Text(text="",modifier = Modifier.height(80.dp))}
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MyTabsPreview() {
//    FriendsTabs()
//}
