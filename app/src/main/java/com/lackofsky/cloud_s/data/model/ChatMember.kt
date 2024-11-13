package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "chat_members",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["uniqueId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Chat::class, parentColumns = ["chatId"], childColumns = ["chatId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["chatId"]), Index(value = ["userId"])]
)
data class ChatMember(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "userId")
    val userId: String,    // References User.uniqueId
    @ColumnInfo(name = "chatId")
    val chatId: String,    // References Chat.id
    @ColumnInfo(name = "role")
    val role: ChatRole,       // e.g., "admin", "participant"
    @ColumnInfo(name = "roleName")
    val roleName: String = "default"
)

enum class ChatRole {
    PARTICIPANT, ADMIN
}