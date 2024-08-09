package com.lackofsky.cloud_s.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lackofsky.cloud_s.data.model.User

@Dao
interface UserDao {
//    @Query("SELECT * FROM users")
//    fun getUsers(): LiveData<List<User>>
//    @Query("SELECT * FROM users WHERE userId = :id")
//    fun getUser(id:Int): LiveData<User>

    @Insert
    fun addUser(user: User)

//    @Query("DELETE FROM users WHERE userId = :id")
//    fun deleteUser(id:Int)
    //TODO
//    @Update
//    fun updateUser(id:Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User): Long
    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE userId = :id")
    fun getUserById(id: Int): LiveData<User>

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE userId = 1")
    fun getUserOwner(): LiveData<User>
}