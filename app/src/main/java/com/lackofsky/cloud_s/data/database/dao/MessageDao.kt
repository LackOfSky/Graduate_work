package com.lackofsky.cloud_s.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.SyncStatus

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun getMessagesByChat(chatId: String): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE messageUniqueId = :messageUniqueId")
    fun getMessageById(messageUniqueId: String): LiveData<Message>

    @Query("SELECT * FROM messages WHERE syncStatus = :status")
    fun getMessagesBySyncStatus(status: SyncStatus): LiveData<List<Message>>

    @Query("UPDATE messages SET syncStatus = :status WHERE messageUniqueId = :messageUniqueId")
    suspend fun updateMessageSyncStatus(messageUniqueId: String, status: SyncStatus)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId ")
    suspend fun getMessagesCount(chatId: String):Long
}