package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.sql.Time

@Entity(tableName = "messages")
data class Message (
    @PrimaryKey
    @ColumnInfo(name = "messageId") @NotNull
    val id: Int,
    @ColumnInfo(name = "chatId") @NotNull
    val chatId: String,
    @ColumnInfo(name = "senderMac") @NotNull
    val senderMac: String,
    @ColumnInfo(name = "messageContent") @NotNull
    val content: String,
    @ColumnInfo(name = "time") @NotNull
    val createdAt: Long = System.currentTimeMillis()
)
