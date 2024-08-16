package com.lackofsky.cloud_s.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lackofsky.cloud_s.data.dao.ChatDao
import com.lackofsky.cloud_s.data.dao.MessageDao

import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatParticipant
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User

import com.lackofsky.cloud_s.data.model.UserInfo

@Database(entities = [User::class,ChatParticipant::class, UserInfo::class, Chat::class,  Message::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao
}