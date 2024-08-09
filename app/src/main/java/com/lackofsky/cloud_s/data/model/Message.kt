package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.sql.Time

@Entity(tableName = "messages")
data class Message (
    @ColumnInfo(name = "messageId") @NotNull
    val id: Int,
    @ColumnInfo(name = "messageAuthor") @NotNull
    val author: String,
    @ColumnInfo(name = "userId") @NotNull
    val time: Time
)
