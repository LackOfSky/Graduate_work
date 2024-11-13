package com.lackofsky.cloud_s.data.repository

import com.lackofsky.cloud_s.data.dao.ReadMessageDao
import com.lackofsky.cloud_s.data.model.ReadMessage

class ReadMessageRepository(private val readMessageDao: ReadMessageDao) {
    suspend fun markMessageAsRead(readMessage: ReadMessage) {
        readMessageDao.markMessageAsRead(readMessage)
    }

    suspend fun isMessageRead(messageId: String, userId: String): Boolean {
        return readMessageDao.isMessageReadByUser(messageId, userId) > 0
    }
}