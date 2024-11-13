package com.lackofsky.cloud_s.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lackofsky.cloud_s.data.model.ReadMessage

@Dao
interface ReadMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markMessageAsRead(readMessage: ReadMessage)

    @Query("SELECT COUNT(*) FROM read_messages WHERE messageId = :messageId AND userId = :userId")
    suspend fun isMessageReadByUser(messageId: String, userId: String): Int
}