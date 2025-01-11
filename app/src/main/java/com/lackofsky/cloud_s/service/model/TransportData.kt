package com.lackofsky.cloud_s.service.model

data class TransportData(
    val messageType: MessageType,
    val senderId: String,
    val ownServerPort: Int = 0,
    var senderIp: String = "",
    val sender: String,
    val content: String
)