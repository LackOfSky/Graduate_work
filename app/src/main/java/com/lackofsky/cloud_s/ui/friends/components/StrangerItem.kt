package com.lackofsky.cloud_s.ui.friends.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.ui.friends.FriendsViewModel
import kotlinx.coroutines.delay

//import com.lackofsky.cloud_s.services.p2pService.HostUser

@Composable
fun StrangerItem(userStranger: User, viewModel: FriendsViewModel,isRequested:Boolean){
    var isPeerRequested by remember { mutableStateOf(isRequested) }
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
                text = userStranger.fullName,
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
                text = userStranger.login,
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
        if(!isPeerRequested){
            TextButton(
                onClick = {
                    isPeerRequested = viewModel.sendFriendRequest(userStranger)
                    //isPeerRequested = !isPeerRequested
                    //isPeerRequested = viewModel.isPeerInRequested(userStranger)
                },
                elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp, // Set horizontal padding to 20dp
                    vertical = 5.dp // Set vertical padding to 10dp
                ), border = BorderStroke(1.dp, Color.Transparent),
                modifier = Modifier.background(Color.Transparent, RectangleShape)

            ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_24),
                        contentDescription = "Icon",
                        modifier = Modifier
                        //tint = Color(0xff1d1b20)
                    )
            }
        }else {
                TextButton(
                    onClick = {
                        isPeerRequested = !viewModel.cancelFriendRequest(userStranger)
                    },
                    elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp, // Set horizontal padding to 20dp
                        vertical = 5.dp // Set vertical padding to 10dp
                    ), border = BorderStroke(1.dp, Color.Transparent),
                    modifier = Modifier.background(Color.Transparent, RectangleShape)

                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.baseline_check_16),
                        contentDescription = "Icon",
                        modifier = Modifier
                        //tint = Color(0xff1d1b20)
                    )
                }
        }

    }
}

@Composable
fun PendingStrangerItem(userStranger: User, viewModel: FriendsViewModel){
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
                text = userStranger.fullName,
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
                text = userStranger.login,
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
        Row(){
            TextButton(
                onClick = {
                    viewModel.rejectFriendRequest(userStranger)
                },
                elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp, // Set horizontal padding to 20dp
                    vertical = 5.dp // Set vertical padding to 10dp
                ), border = BorderStroke(1.dp, Color.Transparent),
                modifier = Modifier.background(Color.Transparent, RectangleShape)

            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_clear_20),
                    contentDescription = "Icon",
                    modifier = Modifier
                    //tint = Color(0xff1d1b20)
                )
            }
            TextButton(
                onClick = {
                    viewModel.approveFriendRequest(userStranger)
                },
                elevation = ButtonDefaults.elevatedButtonElevation(0.dp),
                contentPadding = PaddingValues(
                    horizontal = 16.dp, // Set horizontal padding to 20dp
                    vertical = 5.dp // Set vertical padding to 10dp
                ), border = BorderStroke(1.dp, Color.Transparent),
                modifier = Modifier.background(Color.Transparent, RectangleShape)

            ) {

                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Icon",
                    modifier = Modifier
                    //tint = Color(0xff1d1b20)
                )
            }
        }

    }
}

