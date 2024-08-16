package com.lackofsky.cloud_s.data.repository

import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.dao.MessageDao
import com.lackofsky.cloud_s.data.model.Message
import javax.inject.Inject

class MessageRepository @Inject constructor(
    private val messageDao: MessageDao
) {

    suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message)
    }

    fun getMessagesForChat(chatId: String): LiveData<List<Message>> {
        return messageDao.getMessagesForChat(chatId)
    }

    suspend fun getMessageById(messageId: Int): Message? {
        return messageDao.getMessageById(messageId)
    }
}