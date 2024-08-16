package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    @ColumnInfo(name = "chatName")
    val chatName: String,
    @ColumnInfo(name = "chatId") @NotNull
    val chatId: String, // Можно использовать комбинацию мак-адресов для уникальности
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)