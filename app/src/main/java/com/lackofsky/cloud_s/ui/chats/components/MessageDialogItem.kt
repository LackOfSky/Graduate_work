package com.lackofsky.cloud_s.ui.chats.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.navigation.compose.hiltViewModel
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.ui.chats.ChatDialogViewModel


@Composable
fun MessageDialogItem(message: Message,viewModel: ChatDialogViewModel = hiltViewModel(),
                      isFriendOnline: Boolean = false, isNotesChat: Boolean = true) {
    //рыба
    var isUserOwner by remember { mutableStateOf(viewModel.isFromOwner(message.userId)) }
    var isSelected by remember { mutableStateOf(false) }
    val isSelectingMode by viewModel.isSelectingMode.collectAsState()
    val context = LocalContext.current
    var isExpandedItemMenu by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .defaultMinSize(minHeight = 30.dp)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
                .padding(
                    start = if (isUserOwner) 40.dp else 0.dp,//TODO check user
                    end = if (isUserOwner) 0.dp else 40.dp
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
                colors = CardDefaults.cardColors( if(isSelected) Color.LightGray else Color.White ),
//                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(3.dp, 6.dp)
                    .pointerInput(Unit) {//Clickable
                        detectTapGestures(
                            onTap = {
                                if (isSelectingMode) {
                                    isSelected = !isSelected
                                    viewModel.selectedMessage(message, isSelected)
                                } else {
                                    isExpandedItemMenu = true
                                    //TODO("Логика CRUD-ОПЕРАЦИЙ")
                                }
                            },
                            onLongPress = {
                                if (isSelectingMode.not()) {
                                    isSelected = !isSelected
                                    viewModel.selectedMessage(message, isSelected)
                                }
                            }
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
        DropdownMenu(
            expanded = isExpandedItemMenu,
            onDismissRequest = { isExpandedItemMenu = false },

            offset = DpOffset(80.dp, 0.dp), // Offset to adjust menu position
            modifier = Modifier
                .padding(10.dp)
                .background(Color.Transparent)
                .widthIn(min=100.dp,max=180.dp)
        ) {
            //todo иконки под поля

            DropdownMenuItem(label = "reply", onCLick = {/*TODO()*/true}, drawableIcon = R.drawable.baseline_reply_24)//baseline_reply_16
            Divider()
            DropdownMenuItem(label = "redirect", onCLick = {/*TODO()*/true}, drawableIcon = R.drawable.baseline_redo_24)
            DropdownMenuItem(label = "copy", onCLick = { viewModel.copyToClipboard(context = context,message.content) }, drawableIcon = R.drawable.baseline_content_copy_24)
            if(!isNotesChat){
                DropdownMenuItem(label = "delete for everyone", onCLick = {viewModel.deleteMessage(message)}, drawableIcon = R.drawable.baseline_clear_20)
            }
            DropdownMenuItem(
                label = "delete", onCLick = {
                if (isFriendOnline && isNotesChat.not()){
                    viewModel.deleteMessage(message, forMyself = true)
                }else{
                    viewModel.deleteNotedMessage(message)
                }
                                                                       },
                drawableIcon = R.drawable.baseline_clear_20)
        }
    }
}