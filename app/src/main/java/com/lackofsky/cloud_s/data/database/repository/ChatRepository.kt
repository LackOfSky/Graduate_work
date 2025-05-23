package com.lackofsky.cloud_s.data.database.repository


import androidx.room.Transaction
import com.lackofsky.cloud_s.data.database.dao.ChatDao
import com.lackofsky.cloud_s.data.database.dao.ChatMemberDao
import com.lackofsky.cloud_s.data.database.dao.MessageDao
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatListItem
import com.lackofsky.cloud_s.data.model.ChatMember
import com.lackofsky.cloud_s.data.model.ChatRole
import com.lackofsky.cloud_s.data.model.ChatType
import com.lackofsky.cloud_s.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val chatDao: ChatDao,
    private val chatMemberRepository: ChatMemberRepository,
    private val messageDao: MessageDao
) {

    suspend fun insertChat(chat: Chat):Boolean {
        try{
            chatDao.insertChat(chat)
            return true
        }catch (e:Exception){
            return false
        }
    }

    suspend fun deleteChat(chat: Chat):Boolean{
        try{
            chatDao.deleteChat(chat)
            return true
        }catch (e:Exception){
            return false
        }
    }
    fun getAllChats(): Flow<List<Chat>> {
        return chatDao.getAllChats()
    }

    fun getChatListItems(): Flow<List<ChatListItem>> {
        return chatDao.getChatListItems()
    }

    fun getChatByName(id: String): Flow<Chat> {
        return chatDao.getChatByName(id)
    }
    fun getChatById(id: String): Flow<Chat> {
        return chatDao.getChatById(id)
    }
    @Transaction
    suspend fun createPrivateChat( userId2: String, userOwnerId: String? = null): String {//userId1: String,
        // Check if a private chat between these two users exists
        val existingChat = chatDao.getChatByName(userId2).firstOrNull()//userId1,
        existingChat?.let {
            return existingChat.chatId // Return the existing chat ID
        }
        // Create a new private chat
        val newChat = Chat(
            name = userId2,
            chatId = userId2,
            type = ChatType.PRIVATE,
        )
        if (!insertChat(newChat)) throw Exception("GrimBerry.ChatRepository. Error in function insertChat")
        if (!chatMemberRepository.addChatMember(
                ChatMember(
                    chatId = newChat.chatId,
                    userId = userId2,
                    role = ChatRole.ADMIN
                )
            )
        ) throw Exception("GrimBerry.ChatRepository. Error in function insertChatMember")
        userOwnerId?.let { uniqueId ->
            if (!chatMemberRepository.addChatMember(
                    ChatMember(
                        chatId = newChat.chatId,
                        userId = uniqueId,
                        role = ChatRole.ADMIN
                    )
                )
            ) throw Exception("GrimBerry.ChatRepository. Error in function insertChatMember")
        }
        return newChat.chatId
    }
    suspend fun getPrivateChatIdByUser(userId: String):String?{
        return chatDao.getPrivateChatIdByUser(userId)
    }
    suspend fun deletePrivateChat(chatId: String){
        val chat = chatDao.getChatById(chatId).firstOrNull()
            ?: throw Exception("GrimBerry. ChatRepository failed fun deletePrivateChat when chatDao.getChatById(chatId)")

        messageDao.deleteMessagesByChatId(chatId)
        // Remove users from the ChatMember table
        chatMemberRepository.deleteMembersByChatId(chatId)
        // Finally, delete the chat itself
        chatDao.deleteChat(chat)
    }
}