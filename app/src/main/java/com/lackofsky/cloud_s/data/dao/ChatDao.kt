package com.lackofsky.cloud_s.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatParticipant

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChat(chat: Chat)

    @Insert
    suspend fun insertParticipant(participant: ChatParticipant)

    @Query("SELECT * FROM chats WHERE chatName = :chatName")
    suspend fun getChatByName(chatName: String): Chat

    @Query("SELECT * FROM chatParticipants WHERE chatName = :chatName")
    suspend fun getParticipantsByChatName(chatName: String): List<ChatParticipant>

    @Query("Select * FROM chats")
    fun getAllChats():LiveData<List<Chat>>
}