package com.lackofsky.cloud_s.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    @Update
    suspend fun updateUser(user: User)
    @Delete
    suspend fun deleteUser(user: User)
    @Query("SELECT * FROM users WHERE uniqueID = :uniqueID")
    fun getUserByUniqueID(uniqueID: String): Flow<User>
    @Query("SELECT * FROM users WHERE userId = :id")
    fun getUserById(id: Int): Flow<User>
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserInfo(userInfo: UserInfo)
    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)

    @Query("SELECT * FROM users WHERE userId > 1")
    fun getAllUsers(): Flow<List<User>>
    @Query("SELECT * FROM users WHERE userId = 1")
    fun getUserOwner(): Flow<User>

    @Query("SELECT * FROM usersInfo WHERE userId = :uniqueID")
    fun getUserInfoById(uniqueID: String?): Flow<UserInfo>
//  @Transaction
//    @Query("SELECT * FROM users WHERE userId = :id")
//    fun getUserWithInfoById(id: Int): LiveData<UserWithInfo?>
//
//    data class UserWithInfo(
//        @Embedded val user: User,
//        @Relation(
//            parentColumn = "userId",
//            entityColumn = "userId"
//        )
//        val userInfo: UserInfo?
//    )
}