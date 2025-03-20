package com.lackofsky.cloud_s.ui.chats

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.ui.ShowToast
import com.lackofsky.cloud_s.ui.chats.components.MessageDialogItem

@Composable
fun ChatDialogScreen(chatId: String, viewModel: ChatDialogViewModel = hiltViewModel()){
    viewModel.setChatId(chatId)
    val chat by viewModel.activeChat.collectAsState()
    val isFriendOnline by viewModel.isFriendOnline.collectAsState(initial = false)
    Scaffold(
        bottomBar = {
            BottomLineSend(isFriendOnline = isFriendOnline)
        }
    ) { paddingValues ->
        Log.d("placeholder. TODO:DELETE",paddingValues.toString())

        //MessageListTest(paddingValues)// for Testing ui items
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                //.padding(8.dp, 0.dp, 8.dp, 80.dp)//end = BottomLineSend +8

        ) {
            Row(){
                Text(text = chat?.name.orEmpty())
                Text(text = if (isFriendOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFriendOnline) Color.Green else Color.Gray
                )
            }
            MessagesList(viewModel)
        }
    }
}

@Composable
fun MessagesList(viewModel: ChatDialogViewModel, modifier: Modifier = Modifier) {
    val messagesList by viewModel.messages.collectAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        ) {
        messagesList?.let {
            items(it.toList()) { message ->
                    MessageDialogItem(message = message)
            }
        }
    }
}

//@Composable
//fun ChatDialogScreen(chatId: String, viewModel: ChatDialogViewModel = hiltViewModel()) {
//    viewModel.setChatId(chatId)
//    val messages by viewModel.messages.collectAsState(initial = emptyList())
//    var inputText by remember { mutableStateOf("") }
//
//    val paddingValue = 8.dp // Extracted constant for consistent spacing.
//
//    Scaffold(
//        modifier = Modifier.fillMaxSize(),
//        bottomBar = {
//            BottomLineSend(
//                modifier = Modifier
//                    .padding(all = paddingValue)
//                    .fillMaxWidth(),
//                viewModel = viewModel
//            )
//        }
//    ) { innerPadding ->
//        innerPadding.toString()
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                //.padding(innerPadding)
//                .padding(8.dp, 0.dp, 8.dp, 48.dp)
//
//        ) {
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f) // Ensure MessagesList fills available space dynamically.
//                    .fillMaxWidth(),
//                contentPadding = PaddingValues(bottom = 42.dp) // Space for the input field.
//            ) {
//                messages?.let {
//                items(it) { message ->
//                    MessageDialogItem(message = message)
//                }
//                }
//            }
//        }
//    }
//}





@Composable
fun BottomLineSend(modifier: Modifier = Modifier, viewModel: ChatDialogViewModel = hiltViewModel(), isFriendOnline: Boolean = false) {
    var messageText by remember { mutableStateOf("") }

    var showToast by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(color = Color(0xfff3edf7))
            .padding(bottom = 40.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .fillMaxWidth()
                .requiredHeight(height = 56.dp)
                .background(color = Color(0xffece6f0))
        ) {
            Spacer(
                modifier = Modifier
                    .width(15.dp)
            )

            OutlinedTextField(
                value = messageText, // or any default text value
                onValueChange = { newValue -> messageText = newValue },
                placeholder = {
                    Text(
                        "Enter your text...",
                        modifier = Modifier.fillMaxHeight()
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent, // Убрать рамку
                    focusedBorderColor = Color.Transparent,   // Убрать рамку
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(color = Color.Transparent)
            )
            IconButton(//ButtonSend
                onClick = { if(isFriendOnline) {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                }else {
                    showToast = true
                }

                }

            ) {
                ShowToast("User is offline."){
                    showToast = false
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .requiredSize(size = 48.dp)
                        .padding(all = 8.dp)
                        .clip(shape = RoundedCornerShape(100.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_send_24),//mood
                        contentDescription = "mood",
                        tint = Color(0xff1d1b20))
//                            Icon(
//                                painter = painterResource(id = R.drawable.baseline_more_vert_24),//text_fields
//                                contentDescription = "Icon",
//                                tint = Color(0xff49454f))
                }
            }
            AttachButton()
        }
    }
}
@Composable
fun AttachButton(){
    IconButton(
        onClick = { }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .requiredSize(size = 48.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(100.dp))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(all = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_circle_24),
                        contentDescription = "mood",
                        tint = Color(0xff1d1b20))
                }
            }
        }
    }
}

//@Preview(widthDp = 412, heightDp = 910)
//@Preview(widthDp = 360, heightDp = 800)
//@Composable
//private fun ExamplesMessagingMobilePreview() {
//    ExamplesMessagingMobile(Modifier)
//}