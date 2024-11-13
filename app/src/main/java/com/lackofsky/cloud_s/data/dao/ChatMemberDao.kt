package com.lackofsky.cloud_s.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lackofsky.cloud_s.data.model.ChatMember

@Dao
interface ChatMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMember(chatMember: ChatMember)

    @Query("SELECT * FROM chat_members WHERE chatId = :chatId")
    fun getMembersByChat(chatId: String): LiveData<List<ChatMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMembers(members: List<ChatMember>)

    @Query("DELETE FROM chat_members WHERE chatId = :chatId")
    suspend fun deleteMembersByChatId(chatId: String)
}