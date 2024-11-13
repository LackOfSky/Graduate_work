package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.UUID

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    @ColumnInfo(name = "chatId")
    val chatId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "chatName") val name: String?,// for 1-1 chat name = userName
    @ColumnInfo(name = "type")  val type: ChatType,
)
enum class ChatType {
    PRIVATE,GROUP
}