package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chatParticipants")
data class ChatParticipant(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "participantId")
    val participantId: Long = 0,
    @ColumnInfo(name = "chatName")
    val chatName: String,
    @ColumnInfo(name = "participantMacAddr")
    val participantMacAddr: String
)