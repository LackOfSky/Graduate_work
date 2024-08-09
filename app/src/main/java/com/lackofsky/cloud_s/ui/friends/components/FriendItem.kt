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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.ui.friends.FriendsViewModel
import com.lackofsky.cloud_s.ui.friends.UserRoutes
import com.lackofsky.cloud_s.ui.profile.EditHeaderUserInfo
import com.lackofsky.cloud_s.ui.profile.HeaderUserInfo
import com.lackofsky.cloud_s.ui.profile.UserProfileFeachures

@Composable
fun FriendItem(userFriend: User){
    var isExpandedItemMenu by remember { mutableStateOf(false) }

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
                    painter = painterResource(id = R.drawable.atom_ico),
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
                    Text("Обмен данными", fontSize=18.sp, modifier = Modifier.padding(10.dp))}
                        //todo иконки под поля
                    Text("Закрепить", fontSize=18.sp, modifier = Modifier.padding(10.dp))
                    Text("Редактировать ник", fontSize=18.sp, modifier = Modifier.padding(10.dp))
                    Divider()
                    Text("Удалить", fontSize=18.sp, modifier = Modifier.padding(10.dp))
                }
            }
}