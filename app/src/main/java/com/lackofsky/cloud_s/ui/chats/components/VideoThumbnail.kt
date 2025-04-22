package com.lackofsky.cloud_s.ui.chats.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
    fun VideoPlayerCard(
        uri: Uri,
        modifier: Modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val context = LocalContext.current
        val exoPlayer = remember(uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
            }
        }

        val lifecycleOwner = LocalLifecycleOwner.current
        val lifecycle = lifecycleOwner.lifecycle

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                    Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                    else -> {}
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
                exoPlayer.release()
            }
        }



        Card(
            modifier = modifier,
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                //.aspectRatio(16 / 9f)
                .background(Color.Black)
            ) {
                AndroidView(
                    factory = {
                        PlayerView(it).apply {
                            player = exoPlayer
                            useController = true
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                )

//                IconButton(
//                    onClick = {
//                        isPlaying = !isPlaying
//                        if (isPlaying) {
//                            exoPlayer.playWhenReady = true
//                        } else {
//                            exoPlayer.pause()
//                        }
//                    },
//                    modifier = Modifier
//                        .align(Alignment.Center)
//                        .size(64.dp)
//                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
//                ) {
//                    Icon(
//                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
//                        contentDescription = if (isPlaying) "Pause" else "Play",
//                        tint = Color.White,
//                        modifier = Modifier.size(40.dp)
//                    )
//                }
                var isPlaying by remember { mutableStateOf(false) }

                LaunchedEffect(exoPlayer) {
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                            isPlaying = isPlayingNow
                        }
                    })
                }
                if (!isPlaying) {
                    IconButton(
                        onClick = {
                            isPlaying = true
                            exoPlayer.playWhenReady = true
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }
    }
//    val context = LocalContext.current
//    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
//
//    LaunchedEffect(uri) {
//        withContext(Dispatchers.IO) {
//            val retriever = MediaMetadataRetriever()
//            try {
//                retriever.setDataSource(context, uri)
//                val frame = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST) // 1 секунда
//                bitmap = frame
//            } catch (e: Exception) {
//                e.printStackTrace()
//            } finally {
//                retriever.release()
//            }
//        }
//    }
//
//    bitmap?.let {
//        Box {
//            Image(
//                bitmap = it.asImageBitmap(),
//                contentDescription = "Video thumbnail",
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .clip(RoundedCornerShape(12.dp)),
//                contentScale = ContentScale.Crop
//            )
//            Icon(
//                imageVector = Icons.Default.PlayArrow,
//                contentDescription = "Play",
//                tint = Color.White,
//                modifier = Modifier
//                    .align(Alignment.Center)
//                    .size(48.dp)
//            )
//        }
//    } ?:
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//                .background(Color.Gray),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("Unable to load video...")
//        }

