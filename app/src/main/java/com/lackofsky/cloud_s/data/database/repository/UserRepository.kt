package com.lackofsky.cloud_s.data.database.repository


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.lackofsky.cloud_s.data.database.dao.UserDao
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.usecase.FriendRequestUseCase
import com.lackofsky.cloud_s.service.model.MessageType
import com.lackofsky.cloud_s.service.model.TransportData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val userDao: UserDao, private val chatRepository: ChatRepository) {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User){
        val newUser = User(
            uniqueID = user.uniqueID,
            fullName = user.fullName,
            login = user.login,
        )
        userDao.insertUser(newUser)
        chatRepository.createPrivateChat(newUser.uniqueID)

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
    fun getUserInfoById(uniqueID: String?,
                        friendRequestUseCase: FriendRequestUseCase? = null): Flow<UserInfo>{
        /***  #payload
         * make request to client, if userInfo is empty
         * * подумать, куда перенести (в бэкэнде обычно к репозиторию стучит сервис-уровень)*/
        val flowUserInfo = userDao.getUserInfoById(uniqueID)
        CoroutineScope(Dispatchers.IO).launch {
            friendRequestUseCase?.let{
                val user = getUserByUniqueID(uniqueID!!).first()
                if(it.requestUserInfo(user)){
                    Log.i("GrimBerry UserRepository", "requested user info")
                }
            }
        }
        return flowUserInfo
    }

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    //todo под снос. в дальнейшем получать данные по персональному мак-адресу
    fun getUserOwner(): Flow<User> = userDao.getUserOwner()

}