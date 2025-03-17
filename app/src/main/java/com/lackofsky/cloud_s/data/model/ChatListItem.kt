package com.lackofsky.cloud_s.data.model

import java.util.Date

data class ChatListItem(
    val chatId: String,
    val chatName: String?,     // For group chats, otherwise null
    val chatType: String,
    val lastMessageText: String?,
    val lastMessageDate: Date?,
    val userId: String,
    val userName: String,
    val userAbout: String?,
    val userIcon: ByteArray?
)