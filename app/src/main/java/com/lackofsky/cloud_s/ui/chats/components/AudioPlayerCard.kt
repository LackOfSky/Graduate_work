package com.lackofsky.cloud_s.ui.chats.components

import android.graphics.drawable.Drawable
import android.media.browse.MediaBrowser
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.lackofsky.cloud_s.R
import com.lackofsky.cloud_s.ui.chats.ChatDialogViewModel
import kotlinx.coroutines.delay

//@Composable
//fun AudioPlayerCard(
//    uri: Uri,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    var isPlaying by remember { mutableStateOf(false) }
//    var currentPosition by remember { mutableStateOf(0L) }
//    var duration by remember { mutableStateOf(1L) }
//
//    val exoPlayer = remember(uri) {
//        ExoPlayer.Builder(context).build().apply {
//            val mediaItem = MediaItem.fromUri(uri)
//            setMediaItem(mediaItem)
//            prepare()
//        }
//    }
//
//    LaunchedEffect(exoPlayer) {
//        while (true) {
//            currentPosition = exoPlayer.currentPosition
//            duration = exoPlayer.duration.takeIf { it > 0 } ?: 1L
//            kotlinx.coroutines.delay(300)
//        }
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            exoPlayer.release()
//        }
//    }
//
//    var trackWidth by remember { mutableStateOf(1f) }
//
//    Card(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(8.dp),
//        elevation = CardDefaults.cardElevation(4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
//                .height(200.dp)
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column {
//                    Text(
//                        text = "Аудіофайл",
//                        style = MaterialTheme.typography.bodyLarge
//                    )
//                    Text(
//                        text = uri.lastPathSegment ?: "Unknown",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = Color.Gray
//                    )
//                }
//
//                IconButton(onClick = {
//                    isPlaying = !isPlaying
//                    exoPlayer.playWhenReady = isPlaying
//                }) {
//                    Icon(
//                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
//                        contentDescription = if (isPlaying) "Pause" else "Play",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            // Прогрес доріжки з підтримкою тапу/перетягування
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 8.dp)
//                    .height(6.dp)
//                    .background(
//                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
//                        shape = MaterialTheme.shapes.extraSmall
//                    )
//                    .onGloballyPositioned {
//                        trackWidth = it.size.width.toFloat()
//                    }
//                    .pointerInput(duration) {
//                        detectTapGestures { offset ->
//                            val percent = (offset.x / trackWidth).coerceIn(0f, 1f)
//                            val newPosition = (duration * percent).toLong()
//                            exoPlayer.seekTo(newPosition)
//                            currentPosition = newPosition
//                        }
//                    }
//            ) {
//                // Заповнений прогрес
//                val progress = (currentPosition / (duration.takeIf { it > 0 } ?: 1L).toFloat())
//                    .coerceIn(0f, 1f)
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .fillMaxWidth(progress)
//                        .background(
//                            color = MaterialTheme.colorScheme.primary,
//                            shape = MaterialTheme.shapes.extraSmall
//                        )
//                )
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 4.dp),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = formatMillis(currentPosition),
//                    style = MaterialTheme.typography.labelSmall
//                )
//                Text(
//                    text = formatMillis(duration),
//                    style = MaterialTheme.typography.labelSmall
//                )
//            }
//        }
//    }
//}
//
//private fun formatMillis(ms: Long): String {
//    val totalSeconds = ms / 1000
//    val minutes = totalSeconds / 60
//    val seconds = totalSeconds % 60
//    return "%d:%02d".format(minutes, seconds)
//}
@Composable
fun AudioPlayerCard(
    uri: Uri,
    modifier: Modifier = Modifier,
    viewModel: ChatDialogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var userIsSeeking by remember { mutableStateOf(false) }

    val audioInfo = viewModel.getAudioMetadata(context, uri)

    val exoPlayer = remember(uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // Обновляем состояние проигрывателя каждую секунду
    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            if (!userIsSeeking) {
                playbackPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration.takeIf { it > 0 } ?: duration
            }
            delay(500)
        }
    }
    // Управление воспроизведением
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.playWhenReady = true
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp)
            .height(80.dp),
        shape = RoundedCornerShape(9.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier
                .padding(2.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audioInfo.first ?:"unknown author",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = audioInfo.second ?: "unknown title",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Slider(
                value = if (duration > 0) playbackPosition / duration.toFloat() else 0f,
                onValueChange = { value ->
                    userIsSeeking = true
                    playbackPosition = (value * duration).toLong()
                },
                onValueChangeFinished = {
                    userIsSeeking = false
                    exoPlayer.seekTo(playbackPosition)
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp).padding(8.dp)
            )
        }
    }
}

