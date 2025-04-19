package com.lackofsky.cloud_s.ui.chats

import android.net.Uri
import android.util.Log
import android.widget.ImageButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.ui.ShowToast
import com.lackofsky.cloud_s.ui.chats.components.AudioPlayerCard
import com.lackofsky.cloud_s.ui.chats.components.DocumentFileCard
import com.lackofsky.cloud_s.ui.chats.components.ImageFileCard
import com.lackofsky.cloud_s.ui.chats.components.MessageDialogItem
import com.lackofsky.cloud_s.ui.chats.components.VideoPlayerCard

@Composable
fun ChatDialogScreen(chatId: String, viewModel: ChatDialogViewModel = hiltViewModel()){
    viewModel.setChatId(chatId)
    val activeUser by viewModel.activeUserOne2One.collectAsState()
    val isFriendOnline by viewModel.isFriendOnline.collectAsState(initial = false)
    val isNotesChat by viewModel.isNotesChat.collectAsState(initial = true)
    Scaffold(
        bottomBar = {
            BottomLineSend(isFriendOnline = isFriendOnline, isNotesChat = isNotesChat)
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
            if(!isNotesChat){
                Row(
                    Modifier
                        .padding(end = 16.dp)
                        .align(Alignment.CenterHorizontally),){
                    Text(text = activeUser?.fullName.orEmpty(),style = MaterialTheme.typography.titleLarge)
                    Text(text = if (isFriendOnline) "Online" else "Offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFriendOnline) Color.Green else Color.Gray
                    )
                }
                Divider()
            }

            MessagesList(viewModel, isFriendOnline = isFriendOnline, isNotesChat = isNotesChat)
            PinnedMedia(viewModel = viewModel)
        }
    }
}

@Composable
fun MessagesList(viewModel: ChatDialogViewModel, modifier: Modifier = Modifier,
                 isFriendOnline: Boolean = false, isNotesChat: Boolean = true) {
    val messagesList by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    var isAtBottom by remember { mutableStateOf(true) }

    // Следим за положением списка
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visibleItems ->
                val lastIndex = listState.layoutInfo.totalItemsCount - 1
                if (visibleItems.isNotEmpty()) {
                    isAtBottom = visibleItems.last().index >= lastIndex - 1
                }
            }
    }

    // Автоскроллим, если пользователь был внизу
    LaunchedEffect(messagesList) {
        if (isAtBottom && !messagesList.isNullOrEmpty()) {
            listState.animateScrollToItem(messagesList!!.size -1)
        }
    }
    LazyColumn(modifier = Modifier
        .fillMaxSize(),
        state = listState,
        ) {
        messagesList?.let {
            items(it) { message ->
                    MessageDialogItem(message = message, isFriendOnline = isFriendOnline, isNotesChat = isNotesChat)
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
fun BottomLineSend(modifier: Modifier = Modifier, viewModel: ChatDialogViewModel = hiltViewModel(),
                   isFriendOnline: Boolean = false, isNotesChat: Boolean = true) {
    var messageText by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }
    val isMediaAttached by viewModel.isMediaAttached.collectAsState()
    val context = LocalContext.current
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
                onClick = { if(isFriendOnline or isNotesChat) {
                    if (messageText.isNotBlank() || isMediaAttached) {
                        viewModel.sendMessage(context, messageText)
                        messageText = ""
                    }
                }else {
                    showToast = true
                }

                }

            ) {
                if(showToast) ShowToast("User is offline."){ showToast = false }
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
            AttachButton(viewModel)
        }
    }
}
@Composable
fun AttachButton(viewModel: ChatDialogViewModel){
    var showMenu by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.attachMedia(uri)
    }

    Box {
        IconButton(onClick = { showMenu = true }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.requiredSize(size = 48.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(shape = RoundedCornerShape(100.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_add_circle_24),
                        contentDescription = "add media",
                        tint = Color(0xff1d1b20),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(text = { Text("Image") }, onClick = {
                showMenu = false
                pickerLauncher.launch("image/*")
            })
            DropdownMenuItem(text = { Text("Video") }, onClick = {
                showMenu = false
                pickerLauncher.launch("video/*")
            })
            DropdownMenuItem(text = { Text("Audio") }, onClick = {
                showMenu = false
                pickerLauncher.launch("audio/*")
            })
            DropdownMenuItem(text = { Text("Document") }, onClick = {
                showMenu = false
                pickerLauncher.launch("*/*") // или application/pdf, application/msword и т.п.
            })
        }
    }

//    IconButton(
//        onClick = {imagePickerLauncher.launch("image/*") }
//    ) {
//        Column(
//            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .requiredSize(size = 48.dp)
//        ) {
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
//                verticalAlignment = Alignment.CenterVertically,
//                modifier = Modifier
//                    .clip(shape = RoundedCornerShape(100.dp))
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .padding(all = 8.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.baseline_add_circle_24),
//                        contentDescription = "mood",
//                        tint = Color(0xff1d1b20))
//                }
//            }
//        }
//    }
}

@Composable
fun PinnedMedia(viewModel: ChatDialogViewModel) {
    val isMediaAttached by viewModel.isMediaAttached.collectAsState()
    val uriItem by viewModel.uriItem.collectAsState()
    Log.d("GrimBerry", "uriItem: $uriItem")
    if (isMediaAttached ) {//&& uriItem != null
        val context = LocalContext.current
        val mimeType = remember(uriItem) {
            context.contentResolver.getType(uriItem!!)
        }
        Card(
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
//                .padding(8.dp)
                .fillMaxWidth()
                .requiredHeight(height = 320.dp)
                .padding(1.dp, 2.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp,0.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Pinned media", modifier = Modifier.padding(start = 5.dp))
                    IconButton(
                        onClick = { viewModel.attachMedia(null) },
                        content = {
                            Icon(
                            painter = painterResource(id = R.drawable.baseline_clear_20),
                            contentDescription = "Clear",
                            tint = Color.Black)
                        }
                    )
                }

                when {
                    mimeType?.startsWith("image/") == true -> {
                        Log.d("GrimBerry", "mimeType: $mimeType")
                        ImageFileCard(uri = uriItem!!, modifier = Modifier.size(80.dp, 80.dp))
                    }

                    mimeType?.startsWith("video/") == true -> {
                        VideoPlayerCard(uri = uriItem!!, modifier = Modifier.size(80.dp, 80.dp))
                    }

                    mimeType?.startsWith("audio/") == true -> {
                        AudioPlayerCard(uri = uriItem!!)
                    }

                    else -> {
                        DocumentFileCard(uri = uriItem!!)
                    }
                }
            }
        }
    }
}

//@Composable
//fun PinnedMedia(viewModel: ChatDialogViewModel){
//    val isMediaAttached by viewModel.isMediaAttached.collectAsState()
//    val uriItem by viewModel.uriItem.collectAsState()
//
//    if (isMediaAttached){
//        Card(//navigate
//            elevation = CardDefaults.cardElevation(10.dp),
//            colors = CardDefaults.cardColors(Color.White),
//            shape = RoundedCornerShape(20.dp),
//            modifier = Modifier.requiredHeight(height = 200.dp)
////                .height(80.dp)
////                .padding(1.dp, 2.dp)
////                .clickable {
////
////                }
//        ) {
//            Column() {
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    Text("pinned: ",
//                        Modifier
//                            .height(25.dp)
//                            .padding(start = 10.dp, top = 5.dp))
//                    IconButton(
//                        onClick = { viewModel.attachMedia(null) },
//                        modifier = Modifier
//                            .align(Alignment.TopEnd)
//                            .requiredSize(25.dp)
//                            .padding(end = 10.dp, top = 5.dp)
//
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.baseline_clear_20),
//                            contentDescription = "stash_Pinned",
//                            tint = Color(0xFF1D1B20)
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(4.dp))
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier
//                        .padding(10.dp)
//                        .requiredHeight(height = 160.dp)
//                       // .background(color = Color(0xffece6f0))
//                ) {
//                    //TODO TODODODO
//                    Image(
//                        painter = rememberAsyncImagePainter(model = uriItem),
//                        contentDescription = "User Icon",
//                        contentScale = ContentScale.Fit,
//                        modifier = Modifier
//                            .heightIn(max = 150.dp)
//                            .widthIn(max = 150.dp)
//                    )
//                }
//            }
//        }
//    }
//}
