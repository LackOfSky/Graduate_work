package com.lackofsky.cloud_s.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lackofsky.cloud_s.data.database.dao.ChatDao
import com.lackofsky.cloud_s.data.database.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.database.dao.MessageDao
import com.lackofsky.cloud_s.data.database.dao.ReadMessageDao

import com.lackofsky.cloud_s.data.database.dao.UserDao
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatMember
import com.lackofsky.cloud_s.data.model.DateTypeConverter
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.ReadMessage
import com.lackofsky.cloud_s.data.model.User

import com.lackofsky.cloud_s.data.model.UserInfo

@Database(entities = [User::class, ChatMember::class, UserInfo::class, Chat::class,  Message::class, ReadMessage::class], version = 7)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
    abstract fun chatMemberDao(): ChatMemberDao
    abstract fun readMessageDao(): ReadMessageDao
}