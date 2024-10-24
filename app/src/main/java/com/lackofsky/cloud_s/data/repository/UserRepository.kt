package com.lackofsky.cloud_s.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao) {
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    suspend fun isUserExists(): Boolean {
        return userDao.getUserCount()>0
    }

    suspend fun insertUserInfo(userInfo: UserInfo) = userDao.insertUserInfo(userInfo)
    suspend fun updateUserInfo(userInfo: UserInfo) = userDao.updateUserInfo(userInfo)

    fun getUserById(id: Int): LiveData<User> = userDao.getUserById(id)
    suspend fun getUserByUniqueID(uniqueID: String): LiveData<User> = userDao.getUserByUniqueID(uniqueID)
    fun getUserInfoById(id: Int): LiveData<UserInfo> = userDao.getUserInfoById(id)

    fun getAllUsers(): LiveData<List<User>> = userDao.getAllUsers()

    //todo под снос. в дальнейшем получать данные по персональному мак-адресу
    fun getUserOwner(): LiveData<User> = userDao.getUserOwner()

}