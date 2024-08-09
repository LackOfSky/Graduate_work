package com.lackofsky.cloud_s.data.repository

import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {
    suspend fun insert(user: User) = userDao.insert(user)
    suspend fun update(user: User) = userDao.update(user)
    suspend fun delete(user: User) = userDao.delete(user)
    fun getUserById(id: Int): LiveData<User> = userDao.getUserById(id)
    fun getAllUsers(): LiveData<List<User>> = userDao.getAllUsers()
    fun getUserOwner(): LiveData<User> = userDao.getUserOwner()

}