package com.lackofsky.cloud_s.data.repository

import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.dao.MessageDao
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.SyncStatus
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
    }

    fun getMessageById(messageId: Int): LiveData<Message> {
        return messageDao.getMessageById(messageId)
    }

    fun getMessagesByChat(chatId: String): LiveData<List<Message>> {
        return messageDao.getMessagesByChat(chatId)
    }

    fun getPendingMessages(): LiveData<List<Message>> {
        return messageDao.getMessagesBySyncStatus(SyncStatus.PENDING)
    }

    suspend fun markMessageAsSynced(messageId: String) {
        messageDao.updateMessageSyncStatus(messageId, SyncStatus.SYNCED)
    }
}