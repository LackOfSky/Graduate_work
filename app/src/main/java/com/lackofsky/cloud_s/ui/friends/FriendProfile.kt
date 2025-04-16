package com.lackofsky.cloud_s.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.ui.profile.DefaultBanner
import com.lackofsky.cloud_s.ui.profile.DefaultIcon
import com.lackofsky.cloud_s.ui.profile.UserProfileFeachures
import java.io.File

@Composable
fun FriendProfile(modifier: Modifier = Modifier,userId: Int,
                  viewModel: FriendViewModel = hiltViewModel()){
    val user by viewModel.getFriend(userId).collectAsState(null)
    val userInfo by viewModel.getFriendInfo(user?.uniqueID.orEmpty()).collectAsState(null)
//    viewModel.setCurrentFriend(userId)
//    //TODO USERSERVICE GET BY USER ID
//    val selectedUser by viewModel.getCurrentUser(userId).collectAsState()

    LazyColumn( Modifier.fillMaxSize()){
        item {
            Card(
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(Color.White),
                modifier = modifier.height(150.dp)
            ) {
                userInfo?.let {
                    it.bannerImgURI?.let { uri ->
                        if(File(uri).exists()){
                            Image(
                                painter = rememberAsyncImagePainter(model = uri),
                                contentDescription = "User Banner",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape = RoundedCornerShape(0.dp)),
                                contentScale = ContentScale.FillBounds
                            )
                        }else{
                            DefaultBanner(userInfo = userInfo)
                        }
                    }?: DefaultBanner(userInfo = userInfo)
                } ?: DefaultBanner(userInfo = userInfo)

            }
        }
        item{
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

                    userInfo?.let{
                        it.iconImgURI?.let { uri ->
                            if(File(uri).exists()){
                                Image(
                                    painter = rememberAsyncImagePainter(model = uri),
                                    contentDescription = "User Ico",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }else{
                                DefaultIcon(userInfo = userInfo)
                            }
                        } ?: DefaultIcon(userInfo = userInfo)
                    } ?: DefaultIcon(userInfo = null)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
                        modifier = Modifier
                            .requiredHeight(height = 136.dp)
                            .weight(weight = 8f)
                    ) {
                        HeaderFriendInfo(selectedUser = user)
                        UserProfileFeachures()
                    }
                }
            }
        }
        item{
            FriendInfoContent(userInfo)
        }
    }
}

@Composable
fun HeaderFriendInfo(modifier: Modifier = Modifier, selectedUser: User?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(){
            Text(
                text = selectedUser?.fullName.orEmpty(),
                color = Color(0xff1d1b20),
                textAlign = TextAlign.Left,
                lineHeight = 1.33.em,
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically))
        }
        Text(
            text = selectedUser?.login.orEmpty(),
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
fun FriendInfoContent( userInfo: UserInfo?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        AboutFriend(userInfo)
        FriendInfo(userInfo)
    }
}

@Composable
fun AboutFriend( userInfo: UserInfo?){
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
        }
        Text(
            text = userInfo?.about.orEmpty() ,
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
fun FriendInfo( userInfo: UserInfo?){
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
        }
        Text(
            text = userInfo?.info.orEmpty(),
            color = Color(0xff1d1b20),
            lineHeight = 1.33.em,
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
        )
    }



}