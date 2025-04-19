package com.lackofsky.cloud_s.ui.chats.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun ImageFileCard(uri: Uri, modifier: Modifier = Modifier.size(200.dp, 200.dp)) {
    val context = LocalContext.current
    Box(modifier = modifier.size(200.dp, 200.dp)) {
        Image(
            painter = rememberAsyncImagePainter(model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build()
            ),
            contentDescription = "Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}