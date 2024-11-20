package com.lackofsky.cloud_s.ui.chats.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.ui.chats.ChatDialogViewModel


@Composable
fun MessageDialogItem(message: Message,viewModel: ChatDialogViewModel = hiltViewModel()) {
    //рыба

    Row(modifier = Modifier.fillMaxWidth()
    ) {
//        Column(
//            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
//            horizontalAlignment = Alignment.End,
//            modifier = Modifier
//                .background(color = Color(0xfffef7ff))
//                .padding(
//                    horizontal = 16.dp,
//                    vertical = 0.dp
//                )
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .clip(
//                        shape = RoundedCornerShape(
//                            topStart = 20.dp,
//                            topEnd = 20.dp,
//                            bottomStart = 20.dp,
//                            bottomEnd = 8.dp
//                        )
//                    )
//                    .background(color = Color(0xff625b71))
//                    .padding(
//                        horizontal = 16.dp,
//                        vertical = 8.dp
//                    )
//            ) {
//                Text(
//                    text = message.content,//TODO()
//                    color = Color.White,
//                    lineHeight = 1.5.em,
//                    style = MaterialTheme.typography.bodyLarge,
//                    modifier = Modifier
//                        .fillMaxWidth())
//            }

        ///
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .defaultMinSize(minHeight = 30.dp)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
                .padding(
                    start = if (true) 40.dp else 0.dp,//TODO check user
                    end = if (true) 0.dp else 40.dp
                )
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 8.dp
                    )
                )
           // ,horizontalArrangement = if (true) Arrangement.End else Arrangement.Start

        ) {
            Card(//navigate
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(Color.White),
//                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(3.dp, 6.dp)
                    .pointerInput(Unit) {//Clickable
                        detectTapGestures(
                            onTap = { },
                            onLongPress = { viewModel.selectedMessage(message) }
                        )
                    }
//                    .clip(
//                        shape = RoundedCornerShape(
//                            topStart = 18.dp,
//                            topEnd = 18.dp,
//                            bottomStart = 18.dp,
//                            bottomEnd = 8.dp
//                        )
//                    )
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = message.content,
                        color = if (false) Color.White else Color.Black,
                        lineHeight = 1.8.em,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Text(
                        text = message.sentAt.toString(), // Дата отправки
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}