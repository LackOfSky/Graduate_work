package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.Date

import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["uniqueId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Chat::class, parentColumns = ["chatId"], childColumns = ["chatId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["chatId"]), Index(value = ["userId"])]
)
data class Message (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "messageId")
    val id: Int = 0,
    @ColumnInfo(name = "messageUniqueId")//TODO подумать
    val uniqueId: String?,
    @ColumnInfo(name = "chatId")
    val chatId: String,//nullable for send message    /***chatId = Owner+Sender ID */
    @ColumnInfo(name = "userId")
    val userId: String,//unique user ID
    @ColumnInfo(name = "messageContent")
    val content: String,
    @ColumnInfo(name = "sentAt")
    val sentAt: Date = Date(),
//    val version: Int = 1,
    @ColumnInfo(name = "syncStatus")
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
enum class SyncStatus {
    PENDING, SYNCED, FAILED
}