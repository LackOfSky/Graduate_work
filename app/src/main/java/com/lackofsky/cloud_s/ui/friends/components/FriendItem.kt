package com.lackofsky.cloud_s.ui.friends.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.ui.ShowToast
import com.lackofsky.cloud_s.ui.chats.ChatRoutes
import com.lackofsky.cloud_s.ui.friends.FriendsViewModel
import com.lackofsky.cloud_s.ui.friends.UserRoutes
import com.lackofsky.cloud_s.ui.profile.DefaultBanner
import com.lackofsky.cloud_s.ui.profile.EditHeaderUserInfo
import com.lackofsky.cloud_s.ui.profile.HeaderUserInfo
import com.lackofsky.cloud_s.ui.profile.ProfileViewModel
import com.lackofsky.cloud_s.ui.profile.UserProfileFeachures
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FriendItem(userFriend: User,viewModel: FriendsViewModel = hiltViewModel(),
               navController: NavHostController,
               isOnline:Boolean = false){
    var isExpandedItemMenu by remember { mutableStateOf(false) }
    val friendInfo by viewModel.getUserInfo(userFriend).collectAsState(null)
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
                friendInfo?.let{
                    it.iconImgURI?.let { uri ->
                        if(File(uri).exists()){
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = "User Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }else{
                            Image(
                                painter = painterResource(id = R.drawable.clouds_night_angle20),
                                contentDescription = "Image",
                                modifier = Modifier
                                    .align(alignment = Alignment.Top)
                                    .width(width = 70.dp)
                                    .height(height = 70.dp)
                                    .weight(weight = 2f)
                                    .clip(shape = RoundedCornerShape(28.dp))
                            )
                        }
                    } ?: Image(
                        painter = painterResource(id = R.drawable.clouds_night_angle20),
                        contentDescription = "Image",
                        modifier = Modifier
                            .align(alignment = Alignment.Top)
                            .width(width = 70.dp)
                            .height(height = 70.dp)
                            .weight(weight = 2f)
                            .clip(shape = RoundedCornerShape(28.dp))
                    )
                } ?: Image(
                    painter = painterResource(id = R.drawable.clouds_night_angle20),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val onlineColor = if (isOnline) Color.Green else Color.Gray
                        //val onlineColor =  Color.Green
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(8.dp) // Размер индикатора
                                .clip(RoundedCornerShape(50)) // Круглый индикатор
                                .background(onlineColor) // Цвет индикатора
                        )
                        Text(
                            text = if (isOnline) "Online" else "Offline",
                            //text =  "Online",
                            style = MaterialTheme.typography.bodySmall,
                            color = onlineColor
                        )
                    }

                    Text(
                        text = userFriend.fullName,
                        color = Color(0xff1d1b20),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(12.dp, 0.dp, 4.dp, 4.dp)
                            .requiredWidth(160.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )
                    Text(
                        text = userFriend.login,
                        color = Color(0xff49454f),
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .requiredWidth(150.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically)
                    )

                }
                Row() {
                    Button(
                        onClick = {
                            //TODO()
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color(0xff004D40),       // цвет текста
                            containerColor = Color(0xffffffff)
                        ), elevation = ButtonDefaults.elevatedButtonElevation(1.dp),
                        modifier = Modifier
//                                contentPadding = PaddingValues(
//                                horizontal = 16.dp, // Set horizontal padding to 20dp
//                        vertical = 5.dp // Set vertical padding to 10dp
//                    ), border = BorderStroke(1.dp, Color.Transparent),
//                    modifier = Modifier.background(Color.Transparent, RectangleShape)

                    ) {
                        TextButton(
                            onClick = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(
                                        ChatRoutes.Chat.createRoute(
                                            viewModel.getPrivateChatId(userId = userFriend.uniqueID)
                                        )
                                    )
                                }
                            },
                            elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp, // Set horizontal padding to 20dp
                                vertical = 5.dp // Set vertical padding to 10dp
                            ), border = BorderStroke(1.dp, Color.Transparent),
                            modifier = Modifier.background(Color.Transparent, RectangleShape)

                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_mail_24),
                                contentDescription = "Image",
                                modifier = Modifier
//                    .padding(all = 10.dp)
                                    .width(width = 24.dp)
                                    .height(height = 24.dp)
                                    .clip(shape = RoundedCornerShape(28.dp))
                            )
                            //ChatDialogScreen(chatId = chatId)
                        }
                    }

                    TextButton(
                        onClick = {
                            isExpandedItemMenu = true
                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp, // Set horizontal padding to 20dp
                            vertical = 5.dp // Set vertical padding to 10dp
                        ), border = BorderStroke(1.dp, Color.Transparent),
                        modifier = Modifier.background(Color.Transparent, RectangleShape)

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_more_vert_24),
                            contentDescription = "Icon",
                            modifier = Modifier
                            //tint = Color(0xff1d1b20)
                        )
                    }
                    //
                }


                DropdownMenu(
                    expanded = isExpandedItemMenu,
                    onDismissRequest = { isExpandedItemMenu = false },

                    offset = DpOffset(136.dp, -28.dp), // Offset to adjust menu position
                    modifier = Modifier
                        .padding(10.dp)
                        .background(Color.Transparent)
                ) {
                    val colorList: List<Color> = listOf(Color.White,Color.Cyan,Color.Black)
                    Card(
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ){
                    Text("Обмін данними (втратило актуальність)", fontSize=18.sp, modifier = Modifier.padding(10.dp))
                    }
                        //todo иконки под поля
                    Text("Закріпити (в майбутніх версіях)", fontSize=18.sp, modifier = Modifier.padding(10.dp))
                    TextButton(
                        onClick = {
                            //viewModel.deleteFriend(userFriend)
                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp, // Set horizontal padding to 20dp
                            vertical = 5.dp // Set vertical padding to 10dp
                        ), border = BorderStroke(1.dp, Color.Transparent),
                        modifier = Modifier.background(Color.Transparent, RectangleShape)

                    ) {
                        Text(
                            "edit name (в майбутніх версіях)",
                            fontSize = 18.sp, color = Color.Black
                        )
                    }
                    Divider()
                    TextButton(
                        enabled = !isOnline,
                        onClick = {
                            viewModel.deleteFriend(userFriend)
                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp, // Set horizontal padding to 20dp
                            vertical = 5.dp // Set vertical padding to 10dp
                        ), border = BorderStroke(1.dp, Color.Transparent),
                        modifier = Modifier.background(Color.Transparent, RectangleShape)

                    ) {
                        Text("delete (when user is offline)",
                            fontSize = 18.sp,color = Color.Black)
                    }
                    TextButton(
                        enabled = isOnline,
                        onClick = {
                            viewModel.deleteFriend(userFriend,forAll = true)
                        },
                        elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                        contentPadding = PaddingValues(
                            horizontal = 16.dp, // Set horizontal padding to 20dp
                            vertical = 5.dp // Set vertical padding to 10dp
                        ), border = BorderStroke(1.dp, Color.Transparent),
                        modifier = Modifier.background(Color.Transparent, RectangleShape)

                    ) {
                        Text("delete for all",
                            fontSize = 18.sp,color = if(isOnline) Color.Black else Color.Gray)
                    }
                }
            }
}