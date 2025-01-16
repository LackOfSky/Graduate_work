package com.lackofsky.cloud_s.data.database.repository

import com.lackofsky.cloud_s.data.database.dao.MessageDao
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
    }

    suspend fun deleteMessage(message: Message){
        messageDao.deleteMessage(message)
    }

    fun getMessageByUniqueId(messageUniqueId: String): Flow<Message> {
        return messageDao.getMessageById(messageUniqueId)
    }

    fun getMessagesByChat(chatId: String): Flow<List<Message>> {
        return messageDao.getMessagesByChat(chatId)
    }

    fun getPendingMessages(): Flow<List<Message>> {
        return messageDao.getMessagesBySyncStatus(SyncStatus.PENDING)
    }

    suspend fun markMessageAsSynced(messageId: String) {
        messageDao.updateMessageSyncStatus(messageId, SyncStatus.SYNCED)
    }
    suspend fun getMessagesCount(chatId: String):Long {
        return messageDao.getMessagesCount(chatId)
    }
}