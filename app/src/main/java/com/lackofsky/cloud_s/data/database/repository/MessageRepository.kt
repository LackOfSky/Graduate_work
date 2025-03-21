package com.lackofsky.cloud_s.data.database.repository

import com.lackofsky.cloud_s.data.database.dao.MessageDao
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
//        message.uniqueId?.let{ messageUniqueId ->
//            messageDao.insertMessage(message)
//            return //messageDao.getMessageById( messageUniqueId).first()
//        }
//        val messageId = messageDao.insertMessage(message)
//        messageDao.updateMessageUniqueId(messageId, message.chatId+messageId)
//        return //message.copy(uniqueId = message.chatId+messageId)
    }

    /***first add */
    suspend fun insertAndUpdateMessage(message: Message):Message{
        return messageDao.insertAndUpdateMessageUniqueId(message)
    }

    suspend fun deleteMessage(message: Message){
        messageDao.deleteMessage(message)
    }
    suspend fun deleteMessagesByMessageId(messageUniqueId: String){
        messageDao.deleteMessagesByUniqueId(messageUniqueId)
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
    fun getLastNoteMessage():Flow<Message>{
        return messageDao.getLastNoteMessage()
    }
}