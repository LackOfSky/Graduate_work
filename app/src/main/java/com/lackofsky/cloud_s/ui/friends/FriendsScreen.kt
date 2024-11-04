package com.lackofsky.cloud_s.ui.friends

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.ui.friends.components.FriendItem
import com.lackofsky.cloud_s.ui.friends.components.FriendProfile
import com.lackofsky.cloud_s.ui.profile.ProfileScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import com.lackofsky.cloud_s.ui.friends.components.StrangerItem

//import com.lackofsky.cloud_s.ui.friends.components.StrangerItem

//import androidx.hilt.navigation.compose.hiltViewModel


sealed class UserRoutes(val route: String) {
    object Users : UserRoutes("users")
    object User : UserRoutes("user/{userId}"){
        fun createRoute(userId: Int) = "user/$userId"
    }
}
@Composable
fun FriendsScreen(viewModel: FriendsViewModel = hiltViewModel()) {
    var isPeerRequested by remember { mutableStateOf(false) }
//    val friendPlaceholder by viewModel.currentUser.collectAsState()//TODO friends placeholder
//    val strangers by viewModel.strangers.collectAsState()
    val navController = rememberNavController()
    FriendsTabs(viewModel = viewModel,
        navController = navController)


}
@Composable
fun FriendsTabs(
                viewModel: FriendsViewModel,
                navController: NavHostController) {
    val tabIndex = remember { mutableStateOf(0) }

    NavHost(navController, startDestination = UserRoutes.Users.route) {
        composable(UserRoutes.Users.route) { FriendsContainer(viewModel, navController,tabIndex) }
        composable(
            route = UserRoutes.User.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            FriendProfile(userId = userId)
            }

        }
    }

@Composable
fun FriendsContainer(viewModel: FriendsViewModel,
                     navController: NavHostController,
                     tabIndex: MutableState<Int>){
    Column {
        // Компонент вкладок
        TabRow(selectedTabIndex = tabIndex.value) {
            viewModel.tabTitlesList.forEachIndexed { index, title ->
                Tab(
                    selected = tabIndex.value == index,
                    onClick = { tabIndex.value = index },
                    text = { Text(text = title) }
                )
            }
        }
        // Содержимое текущей вкладки
        when (tabIndex.value) {
            0 -> FriendList(viewModel,navController)
            1 -> PeerList(viewModel,navController)//AddFriends()
            2 -> WaitingForConnection(viewModel,navController)
        }
    }
}

@Composable
fun FriendList(viewModel: FriendsViewModel,navController: NavHostController) {

    val friendList by viewModel.friends.observeAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        friendList?.let {
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
                    FriendItem(friend)
                }
            }
        }
        item{Text(text="",modifier = Modifier.height(80.dp))}
    }
}

@Composable
fun PeerList(viewModel: FriendsViewModel,navController: NavHostController) {
    val peers by viewModel.peers.collectAsState()
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
                    StrangerItem(peer,viewModel,viewModel.isPeerInRequested(peer))

            }
        }
        item{Text(text="",modifier = Modifier.height(80.dp))}
        item{AddFriends()}
    }
}

@Composable
fun AddFriends() {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = "Enter a login",
                    fontSize = 24.sp,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        val login = remember{mutableStateOf("")}
        TextField(
            login.value,
            {login.value = it},
            placeholder = { Text("Enter a name of your friend..") },
            modifier = Modifier.fillMaxWidth()
        )
        Text(text = "or",
            fontSize = 22.sp,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Card {
            TextButton(onClick = { /*TODO*/ }) {
                Text(text = "Scan QR-code",
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WaitingForConnection(viewModel: FriendsViewModel,navController:NavHostController) {
    val requestedPeers by viewModel.requestedStrangers.collectAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        items(requestedPeers.toList()) { peer ->
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
                    StrangerItem(peer,viewModel,true)

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
