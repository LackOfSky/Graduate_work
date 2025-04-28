package com.lackofsky.cloud_s.ui.chats.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.MessageContentType
import com.lackofsky.cloud_s.ui.chats.AttachedReply
import com.lackofsky.cloud_s.ui.chats.ChatDialogViewModel
import com.lackofsky.cloud_s.ui.chats.ReplyItem
import java.io.File

@Composable
fun MessageDialogItem(message: Message, viewModel: ChatDialogViewModel = hiltViewModel(),
                      isFriendOnline: Boolean = false, isNotesChat: Boolean = true
) {
    // рыба
    val isUserOwner by remember { mutableStateOf(viewModel.isFromOwner(message.userId)) }
    var isSelected by remember { mutableStateOf(false) }
    val isSelectingMode by viewModel.isSelectingMode.collectAsState()
    val context = LocalContext.current
    var isExpandedItemMenu by remember { mutableStateOf(false) }

    val animateScrollID by viewModel.animateScrollID.collectAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (animateScrollID == message.uniqueId) Color.LightGray else Color.Transparent,
        animationSpec = tween(durationMillis = 1500) // Анімація зміни кольору
    )

    Row(modifier = Modifier.fillMaxWidth().background(backgroundColor)
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
                    start = if (isUserOwner) 40.dp else 0.dp, // TODO check user
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
        ) {
            Column {
                ReplyItem(message = message, viewModel = viewModel)

            Card( // navigate
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(if (isSelected) Color.LightGray else Color.White),
                modifier = Modifier
                    .padding(start = 3.dp, end = 3.dp, top = 0.dp, bottom =  6.dp)
                    .pointerInput(Unit) { // Clickable
                        detectTapGestures(
                            onTap = {
                                if (isSelectingMode) {
                                    isSelected = !isSelected
                                    viewModel.selectedMessage(message, isSelected)
                                } else {
                                    isExpandedItemMenu = true
                                    // TODO("Логика CRUD-ОПЕРАЦИЙ")
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
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    message.mediaUri?.let { uri ->
                    when (message.contentType) {
                        MessageContentType.TEXT -> {
                            Log.e("GrimBerry mdi", "Message content error. existing mediaUri when MessageContentType.TEXT")
                        }

                        MessageContentType.IMAGE -> {
                            Log.d("GrimBerry mdi", " message mediaUri" + message.mediaUri)
//                            message.mediaUri?.let { uri ->
                            ImageFileCard(uri.toUri())
                            Divider(Modifier.fillMaxWidth())
//                            }
//                            Divider(Modifier.fillMaxWidth())
//                            Text(
//                                text = message.content,
//                                color = if (false) Color.White else Color.Black,
//                                lineHeight = 1.8.em,
//                                style = MaterialTheme.typography.bodyLarge,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                            )
                        }

                        MessageContentType.VIDEO -> {
//                            message.mediaUri?.let { uri ->
                            Log.d("GrimBerry mdi", " message mediaUri vid " + message.mediaUri)
                            Box(modifier = Modifier.size(200.dp, 200.dp)) {
                                VideoPlayerCard(uri = uri.toUri())
                            }
//                            }
                            Divider(Modifier.fillMaxWidth())
                        }

                        MessageContentType.AUDIO -> {
//                            message.mediaUri?.let { uri->
//                                if(File(uri).exists()){
                            Box(modifier = Modifier.wrapContentSize()) {
                                AudioPlayerCard(uri = uri.toUri())
                            }
//                                }

//                            }
                            Divider(Modifier.fillMaxWidth())
                        }

                        MessageContentType.DOCUMENT -> {
//                            message.mediaUri?.let { uri->
//                                if(File(uri).exists()){
                            DocumentFileCard(uri = uri.toUri())
//                                }
//                            }

                        }
                        // Добавьте обработку других типов контента (AUDIO, VIDEO, LOCATION, CONTACT, DOCUMENT) по аналогии
                        else -> {
                            Text(
                                text = "Unsupported content type",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .size(200.dp, 200.dp)//.fillMaxWidth()
                            )
                            Divider(Modifier.fillMaxWidth())
//                            Text(
//                                text = message.content,
//                                color = if (false) Color.White else Color.Black,
//                                lineHeight = 1.8.em,
//                                style = MaterialTheme.typography.bodyLarge,
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                            )
                        }
                    }
                    }
                    if(message.contentType != MessageContentType.TEXT && message.mediaUri == null){
                        Text(
                            text = "File is downloading or damaged",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                        Divider(Modifier.fillMaxWidth())
                    }

                    Text(
                        text = message.content,
                        color = Color.Black,//if (false) Color.White else Color.Black,
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
        DropdownMenu(
            expanded = isExpandedItemMenu,
            onDismissRequest = { isExpandedItemMenu = false },
            offset = DpOffset(80.dp, 0.dp), // Offset to adjust menu position
            modifier = Modifier
                .padding(10.dp)
                .background(Color.Transparent)
                .widthIn(min = 100.dp, max = 180.dp)
        ) {
            DropdownMenuItem(label = "reply", onCLick = {
                viewModel.attachReply(message)
            }, drawableIcon = R.drawable.baseline_reply_24) // baseline_reply_16
            Divider()
            DropdownMenuItem(label = "redirect", onCLick = { /*TODO()*/ true }, drawableIcon = R.drawable.baseline_redo_24)
            DropdownMenuItem(label = "copy", onCLick = { viewModel.copyToClipboard(context = context, message.content) }, drawableIcon = R.drawable.baseline_content_copy_24)
            if (!isNotesChat) {
                DropdownMenuItem(label = "delete for everyone", onCLick = { viewModel.deleteMessage(message) }, drawableIcon = R.drawable.baseline_clear_20)
            }
            DropdownMenuItem(
                label = "delete", onCLick = {
                    if (isFriendOnline && isNotesChat.not()) {
                        viewModel.deleteMessage(message, forMyself = true)
                    } else {
                        viewModel.deleteNotedMessage(message)
                    }
                },
                drawableIcon = R.drawable.baseline_clear_20
            )
            // Добавьте дополнительные опции для медиа, если необходимо
        }
    }
}