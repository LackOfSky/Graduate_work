package com.lackofsky.cloud_s.data.database.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.lackofsky.cloud_s.data.database.dao.UserDao
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao, private val chatRepository: ChatRepository) {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User){
        userDao.insertUser(user)


         //mirror for friends viewModel
            //if(getUserInfoById(user.uniqueID).isInitialized == false){

            //}
            chatRepository.createPrivateChat(user.uniqueID)

    }
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun deleteUser(user: User){
        chatRepository.deletePrivateChat(user.uniqueID)
        userDao.deleteUser(user)
    }
    suspend fun isUserExists(): Boolean {
        return userDao.getUserCount()>0
    }

    suspend fun insertUserInfo(userInfo: UserInfo) = userDao.insertUserInfo(userInfo)
    suspend fun updateUserInfo(userInfo: UserInfo) = userDao.updateUserInfo(userInfo)

    fun getUserById(id: Int): Flow<User> = userDao.getUserById(id)
    fun getUserByUniqueID(uniqueID: String): Flow<User> = userDao.getUserByUniqueID(uniqueID)
    fun getUserInfoById(uniqueID: String?): Flow<UserInfo> = userDao.getUserInfoById(uniqueID)

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    //todo под снос. в дальнейшем получать данные по персональному мак-адресу
    fun getUserOwner(): Flow<User> = userDao.getUserOwner()

}