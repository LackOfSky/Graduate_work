package com.lackofsky.cloud_s.ui.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.User
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import com.lackofsky.cloud_s.data.model.UserDTO
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.ui.profile.components.UserInfoContent
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory

@Composable
fun ProfileScreen(modifier: Modifier = Modifier,
                  viewModel: ProfileViewModel = hiltViewModel()) {

    val isHeaderEdit by viewModel.isHeaderEdit.observeAsState(initial = false)
    //val currentUser by viewModel.currentUser.collectAsState()
    val editUser by viewModel.editUser.observeAsState()
    val editUserInfo by viewModel.editUserInfo.observeAsState()


    editUser?.let {
    LazyColumn( Modifier.fillMaxSize()) {
        item {
            BannerPicker(viewModel = viewModel)
        }
        item {
            Card(
                //elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.height(200.dp),
                colors = CardDefaults.cardColors(Color.White),
                shape = RoundedCornerShape(0.dp)
//            ,border = BorderStroke(1.dp, Color.Green)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.Start),
                    modifier = modifier
                        .fillMaxWidth()
                        .requiredHeight(height = 200.dp)
                        .background(color = Color(0xfffef7ff))
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        )
                ) {
                    ImagePicker(viewModel)
                    //todo editUserInfo.ico
//                    Image(
//                        painter = painterResource(id = R.drawable.atom_ico),
//                        contentDescription = "Image",
//                        modifier = Modifier
//                            .align(alignment = Alignment.Top)
//                            .width(width = 70.dp)
//                            .height(height = 70.dp)
//                            .weight(weight = 2f)
//                            .clip(shape = RoundedCornerShape(28.dp))
//                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
                        modifier = Modifier
                            .requiredHeight(height = 160.dp)
                            .weight(weight = 8f)
                    ) {
                        if (isHeaderEdit) {
                            EditHeaderUserInfo(viewModel = viewModel, currentUser = it)
                        } else {
                            HeaderUserInfo(viewModel = viewModel, currentUser = it)
                        }
                        UserProfileFeachures()
                    }
                }
            }

        }
        item {
            UserInfoContent(viewModel = viewModel, currentUser = it, currentUserInfo = editUserInfo)
        }
    }
    }

}

@Composable
fun HeaderUserInfo(modifier: Modifier = Modifier, viewModel: ProfileViewModel, currentUser: UserDTO) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(){
            Text(
                text = currentUser.fullName,
                color = Color(0xff1d1b20),
                textAlign = TextAlign.Left,
                lineHeight = 1.33.em,
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically))
            Button(
                onClick = {
                    viewModel.closeEdit()
                    viewModel.onCancelUpdate()
                    viewModel.setIsHeaderEdit(true)
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(0xff004D40),       // цвет текста
                    containerColor = Color(0xffffffff)
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)

                ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_mode_edit_24_pencil),
                    contentDescription = "Image",
                    modifier = Modifier
//                    .padding(all = 10.dp)
                        .width(width = 24.dp)
                        .height(height = 24.dp)
                        .clip(shape = RoundedCornerShape(28.dp)))
            }

        }
        Text(
            text = "@"+currentUser.login,
            color = Color(0xff49454f),
            textAlign = TextAlign.Left,
            lineHeight = 1.5.em,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.sp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically))
    }
}
@Composable
fun EditHeaderUserInfo(viewModel: ProfileViewModel, currentUser: UserDTO){
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
            //name
            TextField(value = currentUser.fullName,
                onValueChange = {value -> viewModel.onUserNameChange(value)},
                textStyle = TextStyle(fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.15.sp,
                    color = Color(0xff49454f),
                    textAlign = TextAlign.Left,
                    lineHeight = 1.33.em),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
        //login

        TextField(value = currentUser.login,
            onValueChange = {value -> viewModel.onUserLoginChange(value)},
            textStyle = TextStyle(fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.sp,
                color = Color(0xff49454f),
                textAlign = TextAlign.Left,
                lineHeight = 1.5.em),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
        )
        Row(modifier = Modifier
            .align(Alignment.End)
            .fillMaxSize()){
            Button(
                onClick = {
                    viewModel.closeEdit()
                    viewModel.onCancelUpdate()
                    //viewModel.set
                }, elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(0xff004D40),       // цвет текста
                    containerColor = Color(0xffffffff)
                ),
                modifier = Modifier.height(40.dp)
            ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_clear_24),
                    contentDescription = "Image",
                    modifier = Modifier
                        .width(width = 24.dp)
                        .height(height = 24.dp)
                        .clip(shape = RoundedCornerShape(28.dp)))
            }
            Button(
                onClick = {
                    viewModel.closeEdit()
                    viewModel.onConfirmUpdateNameLogin()
                }, elevation = ButtonDefaults.elevatedButtonElevation(4.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(0xff004D40),       // цвет текста
                    containerColor = Color(0xffffffff)
                ),
                modifier = Modifier
            ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_check_24),
                    contentDescription = "Image",
                    modifier = Modifier
                        .width(width = 24.dp)
                        .height(height = 24.dp)
                        .clip(shape = RoundedCornerShape(28.dp)))
            }
        }

    }
}

@Composable
fun UserProfileFeachures(){
    Row(modifier = Modifier,
        horizontalArrangement = Arrangement.SpaceAround){
        Image(
            painter = painterResource(id = R.drawable.atom_ico),
            contentDescription = "Image",
            modifier = Modifier
                .padding(all = 10.dp)
                .align(alignment = Alignment.Top)
                .width(width = 50.dp)
                .height(height = 50.dp)
                .clip(shape = RoundedCornerShape(28.dp)))
        Image(
            painter = painterResource(id = R.drawable.atom_ico),
            contentDescription = "Image",
            modifier = Modifier
                .padding(all = 10.dp)
                .align(alignment = Alignment.Top)
                .width(width = 50.dp)
                .height(height = 50.dp)
                .clip(shape = RoundedCornerShape(28.dp)))
        Image(
            painter = painterResource(id = R.drawable.atom_ico),
            contentDescription = "Image",
            modifier = Modifier
                .padding(all = 10.dp)
                .align(alignment = Alignment.Top)
                .width(width = 50.dp)
                .height(height = 50.dp)
                .clip(shape = RoundedCornerShape(28.dp)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePicker(viewModel: ProfileViewModel) {
    val imageUri by viewModel.selectedImageUri.collectAsState()
    val userInfo by viewModel.userInfo.observeAsState()
    val context = LocalContext.current
    // Лаунчер для выбора изображения
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setImageUri(uri, context) // Устанавливаем URI через ViewModel
    }

        // Кнопка для выбора изображения
            Card(// #Button
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(Color.White),
                onClick = {imagePickerLauncher.launch("image/*")},
                modifier = Modifier.clip(CircleShape)
            ) {
                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Выбранное изображение",
                        modifier = Modifier
                            .width(width = 70.dp)
                            .height(height = 70.dp)
                            .clip(shape = RoundedCornerShape(28.dp))
                    )
                    viewModel.setImageUri(uri, context = LocalContext.current)
                } ?: DefaultIcon(userInfo)

        }


}
@Composable
fun DefaultIcon(userInfo: UserInfo?){
    var bitmap: Bitmap?
    userInfo?.let {
        try {
            bitmap = BitmapFactory.decodeByteArray(it.iconImg, 0, it.iconImg.size)
        } catch (e: Exception) {
            bitmap = null
        }

        bitmap?.let { Image(bitmap = it.asImageBitmap(),
            contentDescription = "Image",
            modifier = Modifier
                .width(width = 70.dp)
                .height(height = 70.dp)
                .clip(shape = RoundedCornerShape(28.dp)))
        } ?: Image(
            painter = painterResource(id = R.drawable.atom_ico),//default image
            contentDescription = "Image",
            modifier = Modifier
                //.align(alignment = Alignment.Top)
                .width(width = 70.dp)
                .height(height = 70.dp)
                .clip(shape = RoundedCornerShape(28.dp))
        ) //Или предыдущее изображение

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerPicker(viewModel: ProfileViewModel) {
    val imageUri by viewModel.selectedImageUri.collectAsState()
//    val userInfo by viewModel.userInfo.observeAsState()
    val context = LocalContext.current
    // Лаунчер для выбора изображения
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.setBannerUri(context = context,uri) // Устанавливаем URI через ViewModel
    }

    // Кнопка для выбора изображения

    Card(
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(Color.White),
        onClick = {imagePickerLauncher.launch("image/*")},
        modifier = Modifier.height(150.dp)
    ) {
        //todo editUserInfo.banner

        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Выбранное изображение",
                contentScale = ContentScale.FillWidth
            )
            viewModel.setImageUri(uri, context = LocalContext.current)
        } ?: DefaultBanner(viewModel = viewModel)
    }

}
@Composable
fun DefaultBanner(viewModel: ProfileViewModel) {
    val userInfo by viewModel.userInfo.observeAsState()
    var bitmap: Bitmap?
    val context = LocalContext.current

    userInfo?.let {
        try {
            bitmap = viewModel.getBannerBitmap(context)
        } catch (e: Exception) {
            bitmap = null
        }

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Image",
                contentScale = ContentScale.FillWidth
            )
        } ?: Image(
            bitmap = ImageBitmap.imageResource(R.drawable.banner),
            contentScale = ContentScale.FillWidth,
            contentDescription = "BANNER"
        )//getBannerUri

    }
}