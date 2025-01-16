package com.lackofsky.cloud_s.data.database.repository

import com.lackofsky.cloud_s.data.database.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.model.ChatMember
import kotlinx.coroutines.flow.Flow

class ChatMemberRepository(private val chatMemberDao: ChatMemberDao) {
    suspend fun addChatMember(chatMember: ChatMember):Boolean {
        try{
            chatMemberDao.insertChatMember(chatMember)
            return true
        }catch (e:Exception){
            return false
        }
    }

    fun getMembersByChat(chatId: String): Flow<List<ChatMember>> {
        return chatMemberDao.getMembersByChat(chatId)
    }
    suspend fun deleteMembersByChatId(chatId: String):Boolean{
        try{
            chatMemberDao.deleteMembersByChatId(chatId)
            return true
        }catch (e:Exception){
            return false
        }

    }
}