package com.lackofsky.cloud_s.service.model

data class TransportData(
    val messageType: MessageType,
    val senderId: String,
    val senderIp: String = "",
    val content: String
)