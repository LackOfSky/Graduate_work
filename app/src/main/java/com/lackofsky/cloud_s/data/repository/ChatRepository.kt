package com.lackofsky.cloud_s.data.repository

import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.dao.ChatDao
import com.lackofsky.cloud_s.data.model.Chat
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao
) {

    suspend fun insertChat(chat: Chat) {
        chatDao.insertChat(chat)
    }

    suspend fun getChatById(chatId: String): Chat {
        return chatDao.getChatByName(chatId)
    }

    fun getAllChats(): LiveData<List<Chat>> {
        return chatDao.getAllChats()
    }

    suspend fun getChatByName(chatName: String): Chat {
        return chatDao.getChatByName(chatName)
    }
}