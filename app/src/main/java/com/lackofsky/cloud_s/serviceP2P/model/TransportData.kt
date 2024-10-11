package com.lackofsky.cloud_s.serviceP2P.model

data class TransportData(
    val messageType: MessageType,
    val senderId: String,
    val senderIp: String = "",
    val content: String
)