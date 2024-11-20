package com.lackofsky.cloud_s.ui.chats.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatListItem
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.ui.chats.ChatsViewModel

@Composable
fun ChatItem(viewModel: ChatsViewModel = hiltViewModel(), chatListItem: ChatListItem) {
    var isIconExist by remember { mutableStateOf(false) }
    lateinit var bitmap: Bitmap
//    val friendPlaceholder by viewModel.currentUser.collectAsState()//TODO friends placeholder
//    val strangers by viewModel.strangers.collectAsState()
//    val navController = rememberNavController()
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
            try{
                bitmap = BitmapFactory.decodeByteArray(chatListItem.userIcon, 0, chatListItem.userIcon.size)
                isIconExist = true

            }catch (e:Exception){
                isIconExist = false
            }
            if(isIconExist){
                Image(
                    bitmap = bitmap.asImageBitmap(),//            painter = painterResource(id = R.drawable.atom_ico),
                    contentDescription = "Image",
                    modifier = Modifier
                        .align(alignment = Alignment.Top)
                        .width(width = 70.dp)
                        .height(height = 70.dp)
                        .weight(weight = 2f)
                        .clip(shape = RoundedCornerShape(28.dp))
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

            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                modifier = Modifier
                    .weight(weight = 8f)
            ) {
                Text(
                    text = chatListItem.userName,//
                    color = Color(0xff1d1b20),
                    textAlign = TextAlign.Left,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(12.dp, 0.dp, 4.dp, 4.dp)
                        .requiredWidth(160.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                )
                Text(
                    text = chatListItem.lastMessageText.orEmpty(),
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




