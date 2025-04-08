package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import org.jetbrains.annotations.NotNull
import java.util.Date

import java.util.UUID

@Entity(
    tableName = "messages",
    foreignKeys = [
//        ForeignKey(entity = User::class, parentColumns = ["uniqueId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Chat::class, parentColumns = ["chatId"], childColumns = ["chatId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["chatId"]), Index(value = ["userId"])]
)
data class Message (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "messageId")
    val id: Long = 0,
    @ColumnInfo(name = "messageUniqueId")//TODO подумать
    val uniqueId: String? = null,
    @ColumnInfo(name = "chatId")
    val chatId: String,//nullable for send message    /***chatId = Owner+Sender ID */
    @ColumnInfo(name = "userId")
    val userId: String,//unique user ID
    @ColumnInfo(name = "messageContent")
    val content: String = "",
    @ColumnInfo(name = "sentAt")
    val sentAt: Date = Date(),
    @ColumnInfo(name = "syncStatus")
    val syncStatus: SyncStatus = SyncStatus.PENDING,

    //upd
    @ColumnInfo(name = "replyMessageId")
    val replyMessageId: String? = null,
    @ColumnInfo(name = "contentType")
    val contentType: MessageContentType = MessageContentType.TEXT,
    @ColumnInfo(name = "mediaUri")
    val mediaUri: String? = null
)
enum class SyncStatus {
    PENDING, SYNCED, FAILED
}
enum class MessageContentType {
    TEXT, IMAGE, AUDIO, VIDEO, LOCATION, CONTACT, DOCUMENT
}

class DateTypeConverter {

    // Преобразование из Date в Long
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time // Возвращает миллисекунды (epoch time)
    }

    // Преобразование из Long в Date
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
