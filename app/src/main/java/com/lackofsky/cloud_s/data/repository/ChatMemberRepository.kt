package com.lackofsky.cloud_s.data.repository

import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.model.ChatMember

class ChatMemberRepository(private val chatMemberDao: ChatMemberDao) {
    suspend fun addChatMember(chatMember: ChatMember) {
        chatMemberDao.insertChatMember(chatMember)
    }

    suspend fun getMembersByChat(chatId: String): LiveData<List<ChatMember>> {
        return chatMemberDao.getMembersByChat(chatId)
    }
}