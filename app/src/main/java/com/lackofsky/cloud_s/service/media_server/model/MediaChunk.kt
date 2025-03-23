package com.lackofsky.cloud_s.service.media_server.model

data class MediaChunk(
    val fileName: String,
    val chunkIndex: Int,
    val totalChunks: Int,
    val data: ByteArray,
    val isLastChunk: Boolean
)