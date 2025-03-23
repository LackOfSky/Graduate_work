package com.lackofsky.cloud_s.ui.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserDTO
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.ui.profile.ProfileViewModel

@Composable
fun UserInfoContent(viewModel: ProfileViewModel, currentUserInfo: UserInfo?) {
    val isAboutEdit by viewModel.isAboutEdit.collectAsState(initial = false)
    val isInfoEdit by viewModel.isInfoEdit.collectAsState(initial = false)
    currentUserInfo?.let {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            modifier = Modifier
//            .fillMaxWidth()
//            .background(color = Color(0xfffef7ff))
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
        ) {
            if (isAboutEdit) {
                AboutUserEdit(viewModel, currentUserInfo)
            } else {
                AboutUser(viewModel, currentUserInfo)
            }
            if (isInfoEdit) {
                UserInfoEdit(viewModel, currentUserInfo)
            } else {
                UserInfo(viewModel, currentUserInfo)
            }
            Row(modifier = Modifier.height(60.dp)){

            }
        }
    }
}
@Composable
fun AboutUser(viewModel: ProfileViewModel, currentUserInfo: UserInfo){
    Column {
        Row(){
            Text(
                text = "About me",
                color = Color(0xff49454f),
                lineHeight = 1.33.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Bottom)
                    .align(alignment = Alignment.CenterVertically)
            )
            TextButton(
                onClick = {
                    viewModel.closeEdit()
                    viewModel.onCancelUpdate()
                    viewModel.setIsAboutEdit(true)
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Color.Transparent, RectangleShape)
            ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_mode_edit_20_pencil),
                    contentDescription = "Edit_about_me",
                    modifier = Modifier
//                    .padding(all = 10.dp)
//                        .width(width = 24.dp)
//                        .height(height = 24.dp)
                        .clip(shape = RoundedCornerShape(28.dp))
                )
            }
        }
        Text(
            text = currentUserInfo.about ,
            color = Color(0xff1d1b20),
            lineHeight = 1.33.em,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
        )
    }



}

@Composable
fun AboutUserEdit(viewModel: ProfileViewModel, currentUserInfo: UserInfo){
    Column {
        Row() {
            Text(
                text = "About me",
                color = Color(0xff49454f),
                lineHeight = 1.33.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Bottom)
                    .align(alignment = Alignment.CenterVertically)
//                    .align(alignment = Alignment.)
            )
            Row(){
                TextButton(
                    onClick = {
                        viewModel.closeEdit()
                        viewModel.onCancelUpdate()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(Color.Transparent, RectangleShape)
                ){
                    Image(
                        painter = painterResource(id = R.drawable.baseline_clear_20),
                        contentDescription = "Confirm_about_me",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(28.dp)))
                }
                TextButton(
                    onClick = {
                        viewModel.closeEdit()
                        viewModel.onConfirmUpdateAboutUser()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(Color.Transparent, RectangleShape)
                ){
                    Image(
                        painter = painterResource(id = R.drawable.baseline_check_20),
                        contentDescription = "Confirm_about_me",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(28.dp)))
                }
            }
        }
    }
        TextField(
            value = currentUserInfo.about,
            onValueChange = { value -> viewModel.onUserAboutUpdate(value) },
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.sp,
                color = Color(0xff49454f),
                lineHeight = 1.33.em
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
}

@Composable
fun UserInfo(viewModel: ProfileViewModel, currentUserInfo: UserInfo){
    Column {
        Row(){
            Text(
                text = "Addition info",
                color = Color(0xff49454f),
                lineHeight = 1.33.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Bottom)
                    .align(alignment = Alignment.CenterVertically)
            )
            TextButton(
                onClick = {
                    viewModel.closeEdit()
                    viewModel.onCancelUpdate()
                    viewModel.setIsInfoEdit(true)
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Color.Transparent, RectangleShape)
            ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_mode_edit_20_pencil),
                    contentDescription = "Edit_user_info",
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(28.dp))
                )
            }
        }
        Text(
            text = currentUserInfo.info,
            color = Color(0xff1d1b20),
            lineHeight = 1.33.em,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
        )
    }



}

@Composable
fun UserInfoEdit(viewModel: ProfileViewModel, currentUserInfo: UserInfo){
    Column {
        Row() {
            Text(
                text = "Addition info",
                color = Color(0xff49454f),
                lineHeight = 1.33.em,
                style = TextStyle(
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.Bottom)
                    .align(alignment = Alignment.CenterVertically)
//                    .align(alignment = Alignment.)
            )
            Row(){
                TextButton(
                    onClick = {
                        viewModel.closeEdit()
                        viewModel.onCancelUpdate()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(Color.Transparent, RectangleShape)
                ){
                    Image(
                        painter = painterResource(id = R.drawable.baseline_clear_20),
                        contentDescription = "Clear_user_info",
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(28.dp)))
                }
            TextButton(
                onClick = {
                    viewModel.closeEdit()
                    viewModel.onConfirmUpdateUserInfo()
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .background(Color.Transparent, RectangleShape)
            ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_check_20),
                    contentDescription = "Confirm_user_info",
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(28.dp)))
            }
            }
        }
    }
    TextField(
        value = currentUserInfo.info,
        onValueChange = { value -> viewModel.onUserAdditionalInfoUpdate(value) },
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.15.sp,
            color = Color(0xff49454f),
            lineHeight = 1.33.em
        ),
        modifier = Modifier
            .fillMaxWidth()
    )
}
