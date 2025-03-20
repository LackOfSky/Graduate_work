package com.lackofsky.cloud_s.data.database.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Query("UPDATE messages SET messageUniqueId = :uniqueId WHERE messageId = :messageId")
    suspend fun updateMessageUniqueId(messageId: Long, uniqueId: String)

    @Transaction
    suspend fun insertAndUpdateMessageUniqueId(message: Message): Message {
        val messageId = insertMessage(message)
        val uniqueId = message.chatId + messageId
        updateMessageUniqueId(messageId, uniqueId)
        return message.copy(uniqueId = uniqueId)
    }


//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertMessage(message: Message) : Long

    /***
     * частина логіки додання повідомлення. додаємо повідомлення через insertMessage,
     * потім оновлюємо його uniqueId через updateMessageUniqueId
     * */
//    @Query("UPDATE messages SET messageUniqueId = :uniqueId WHERE messageId = :id")
//    suspend fun updateMessageUniqueId(id: Long, uniqueId: String)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun getMessagesByChat(chatId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE messageUniqueId = :messageUniqueId")
    fun getMessageById(messageUniqueId: String): Flow<Message>

    @Query("SELECT * FROM messages WHERE syncStatus = :status")
    fun getMessagesBySyncStatus(status: SyncStatus): Flow<List<Message>>

    @Query("UPDATE messages SET syncStatus = :status WHERE messageUniqueId = :messageUniqueId")
    suspend fun updateMessageSyncStatus(messageUniqueId: String, status: SyncStatus)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE chatId = :chatId ")
    suspend fun getMessagesCount(chatId: String):Long
}