package com.lackofsky.cloud_s.ui.chats

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.ui.chats.components.ChatItem
import com.lackofsky.cloud_s.ui.chats.components.MessageDialogItem
import org.bouncycastle.math.raw.Mod

@Composable
fun ChatDialogScreen(chatId: String, viewModel: ChatDialogViewModel = hiltViewModel()){
    Log.d("GrimBerry CH S,",chatId)
    viewModel.setChatId(chatId)
    Scaffold(
        bottomBar = {
            BottomLineSend()
        }
    ) { paddingValues ->
        Log.d("placeholder. TODO:DELETE",paddingValues.toString())

        //MessageListTest(paddingValues)// for Testing ui items
        MessagesList(viewModel)
            // Поле ввода нового сообщения
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextField(
//                    value = messageText,
//                    onValueChange = { messageText = it },
//                    placeholder = { Text("Введите сообщение...") },
//                    modifier = Modifier.weight(1f)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Button(onClick = {
//                    if (messageText.isNotBlank()) {
//                        viewModel.sendMessage(messageText)
//                        messageText = ""
//                    }
//                }) {
//                    Text("Отправить")
//                }
//            }
        }
}

@Composable
fun MessagesList(viewModel: ChatDialogViewModel) {
    val messagesList by viewModel.messages.observeAsState()
    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp, 0.dp, 8.dp, 0.dp)) {
        messagesList?.let {
            items(it.toList()) { message ->
                    MessageDialogItem(message = message)
            }
        }
    }
}

@Composable
fun BottomLineSend(modifier: Modifier = Modifier, viewModel: ChatDialogViewModel = hiltViewModel()) {
    var messageText by remember { mutableStateOf("") }
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
                //.padding(all = 3.dp)
                //.clip(shape = RoundedCornerShape(28.dp))
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
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                }//messageText
            ) {
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