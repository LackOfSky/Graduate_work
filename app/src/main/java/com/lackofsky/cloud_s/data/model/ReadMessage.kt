package com.lackofsky.cloud_s.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

@Entity(//под СНОС, или обдумать практичность\правильность использования
    tableName = "read_messages",
    primaryKeys = ["messageId", "userId"],
    foreignKeys = [
        ForeignKey(entity = Message::class, parentColumns = ["messageId"], childColumns = ["messageId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["uniqueId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["messageId"]), Index(value = ["userId"])]
)
data class ReadMessage(
    val messageId: String,  // References Message.id
    val userId: String,     // References User.uniqueId
    val readAt: Date = Date()
)