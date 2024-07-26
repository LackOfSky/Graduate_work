package com.lackofsky.cloud_s.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.ui.profile.ProfileViewModel
import androidx.compose.runtime.livedata.observeAsState

val mockUser = User("John Doe", //TODO
    "@just_someone")

@Composable
fun ProfileUserInfo(modifier: Modifier = Modifier, viewModel: ProfileViewModel) {
    val isHeaderEdit by viewModel.isHeaderEdit.observeAsState(initial = false)
    val currentUser by viewModel.currentUser.collectAsState()
    Column {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(Color.White),
            modifier = modifier.height(150.dp)
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(R.drawable.banner),
                contentScale = ContentScale.FillWidth,
                contentDescription = "BANNER")
        }
        Card(
            //elevation = CardDefaults.cardElevation(1.dp),
            colors = CardDefaults.cardColors(Color.White),
            shape = RoundedCornerShape(0.dp)//,
            //border = BorderStroke(1.dp, Color.Transparent)
        ){
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
        Image(
            painter = painterResource(id = R.drawable.atom_ico),
            contentDescription = "Image",
            modifier = Modifier
                .align(alignment = Alignment.Top)
                .width(width = 70.dp)
                .height(height = 70.dp)
                .weight(weight = 2f)
                .clip(shape = RoundedCornerShape(28.dp)))
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            modifier = Modifier
                .requiredHeight(height = 136.dp)
                .weight(weight = 8f)
        ) {
            if (isHeaderEdit) {
                EditHeaderUserInfo(viewModel = viewModel, currentUser)
            }else{
                HeaderUserInfo(viewModel = viewModel, currentUser = currentUser)
            }
            UserProfileFeachures()
        }
    }
    }
        }
    UserInfoContent(viewModel = viewModel, currentUser = currentUser)
}

@Composable
fun HeaderUserInfo(modifier: Modifier = Modifier, viewModel: ProfileViewModel,currentUser: User) {
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
            text = currentUser.login,
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
fun EditHeaderUserInfo(viewModel: ProfileViewModel,currentUser: User){
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(){
            //name
            TextField(value = currentUser.fullName,
                onValueChange = {value -> viewModel.updateName(value)},
                textStyle = TextStyle(fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.15.sp,
                    color = Color(0xff49454f),
                    textAlign = TextAlign.Left,
                    lineHeight = 1.33.em),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically)
            )
            Button(
                onClick = {
                    viewModel.setIsHeaderEdit(false)
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color(0xff004D40),       // цвет текста
                    containerColor = Color(0xffffffff)
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ){
                Image(
                    painter = painterResource(id = R.drawable.baseline_check_24),
                    contentDescription = "Image",
                    modifier = Modifier
//                    .padding(all = 10.dp)
                        .width(width = 24.dp)
                        .height(height = 24.dp)
                        .clip(shape = RoundedCornerShape(28.dp)))
            }

        }
        //login
        TextField(value = currentUser.login,
            onValueChange = {value -> viewModel.updateLogin(value)},
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