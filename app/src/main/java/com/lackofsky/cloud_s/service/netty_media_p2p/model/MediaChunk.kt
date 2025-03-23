package com.lackofsky.cloud_s.service.netty_media_p2p.model

data class MediaChunk(
    val fileName: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val data: ByteArray,
    val isLastChunk: Boolean
)