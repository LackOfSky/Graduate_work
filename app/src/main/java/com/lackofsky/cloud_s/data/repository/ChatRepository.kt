package com.lackofsky.cloud_s.data.repository

import androidx.lifecycle.LiveData
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import com.lackofsky.cloud_s.data.dao.ChatDao
import com.lackofsky.cloud_s.data.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.dao.MessageDao
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatListItem
import com.lackofsky.cloud_s.data.model.ChatMember
import com.lackofsky.cloud_s.data.model.ChatRole
import com.lackofsky.cloud_s.data.model.ChatType
import com.lackofsky.cloud_s.data.model.User
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao, private val chatMemberDao: ChatMemberDao,
    private val messageDao: MessageDao
) {

    suspend fun insertChat(chat: Chat) {
        chatDao.insertChat(chat)
    }

    suspend fun deleteChat(chat: Chat){
        chatDao.deleteChat(chat)
    }

    fun getAllChats(): LiveData<List<ChatListItem>> {
        return chatDao.getChatListItems()
    }
     suspend fun getChatById(id: String): LiveData<Chat> {
        return chatDao.getChatById(id)
    }
    @Transaction
    suspend fun createPrivateChat(userId1: String, userId2: String): String {
        // Check if a private chat between these two users exists
        val existingChat = chatDao.getPrivateChat(userId1, userId2)
        if (existingChat.isInitialized) {
            return existingChat.value!!.chatId // Return the existing chat ID
        }

        // Create a new private chat
        val newChat = Chat(
            name = userId2,
            type = ChatType.PRIVATE,
        )
        chatDao.insertChat(newChat)

        val chatMembers = listOf(
            ChatMember(
                chatId = newChat.chatId,
                userId = userId1,
                role = ChatRole.ADMIN     // Adjust role as needed
            ),
            ChatMember(
                chatId = newChat.chatId,
                userId = userId2,
                role = ChatRole.ADMIN
            )
        )
        chatMemberDao.insertChatMembers(chatMembers)

        return newChat.chatId
    }

    suspend fun deletePrivateChat(chatId: String){
        val chat = chatDao.getChatById(chatId) ?: return
        if(!chat.isInitialized){
            throw Exception("GrimBerry. ChatRepository failed fun deletePrivateChat when chatDao.getChatById(chatId)")
        }

        messageDao.deleteMessagesByChatId(chatId)
        // Remove users from the ChatMember table
        chatMemberDao.deleteMembersByChatId(chatId)
        // Finally, delete the chat itself
        chatDao.deleteChat(chat.value!!)
    }
}