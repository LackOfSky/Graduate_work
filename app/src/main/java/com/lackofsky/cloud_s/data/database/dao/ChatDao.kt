package com.lackofsky.cloud_s.data.database.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatListItem
import com.lackofsky.cloud_s.data.model.ChatType
import com.lackofsky.cloud_s.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface ChatDao {

    @Query("Select * FROM chats")
    fun getAllChats(): Flow<List<Chat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat)
    @Delete
    suspend fun deleteChat(chat: Chat)
    @Update
    suspend fun updateChat(chat: Chat)

    @Query("SELECT * FROM chats WHERE chatId = :chatId")
    fun getChatById(chatId: String): Flow<Chat>

    @Query("SELECT * FROM chats WHERE chatName = :chatName")
    fun getChatByName(chatName: String): Flow<Chat>
    @Query("""
        SELECT cm.chatId 
        FROM chat_members cm
        JOIN chats c ON cm.chatId = c.chatId
        WHERE cm.userId = :userId AND c.type = :type
    """)
    suspend fun getPrivateChatIdByUser(userId: String, type: ChatType = ChatType.PRIVATE): String?

@Query("""
    SELECT 
        c.chatId AS chatId, 
        c.chatName AS chatName,
        c.type AS chatType,
        u.uniqueID AS userId,
        u.userName AS userName,
        ui.userAbout AS userAbout,
        ui.userIcon AS userIcon,
        m.messageContent AS lastMessageText,
        MAX(m.sentAt) AS lastMessageDate
    FROM chats c
    LEFT JOIN chat_members cm ON cm.chatId = c.chatId
    LEFT JOIN users u ON cm.userId = u.uniqueID
    LEFT JOIN usersInfo ui ON ui.userId = u.uniqueID
    LEFT JOIN messages m ON m.chatId = c.chatId
    WHERE u.userId > 1
    GROUP BY c.chatId, u.uniqueID
    ORDER BY MAX(m.sentAt) DESC
""")
fun getChatListItems(): Flow<List<ChatListItem>>
//    @Query("""
//        SELECT * FROM chats
//        WHERE type = 'private'
//          AND chatId IN (
//              SELECT chatId FROM chat_members WHERE userId = :userId1
//          )
//          AND chatId IN (
//              SELECT chatId FROM chat_members WHERE userId = :userId2
//          )
//        LIMIT 1
//    """)
//    fun getPrivateChat(userId1: String, userId2: String): LiveData<Chat>
}