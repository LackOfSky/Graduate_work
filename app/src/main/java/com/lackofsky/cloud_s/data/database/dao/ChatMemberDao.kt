package com.lackofsky.cloud_s.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lackofsky.cloud_s.data.model.ChatMember
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMember(chatMember: ChatMember)

    @Query("SELECT * FROM chat_members WHERE chatId = :chatId")
    fun getMembersByChat(chatId: String): Flow<List<ChatMember>>

    @Query("DELETE FROM chat_members WHERE chatId = :chatId")
    suspend fun deleteMembersByChatId(chatId: String)
}