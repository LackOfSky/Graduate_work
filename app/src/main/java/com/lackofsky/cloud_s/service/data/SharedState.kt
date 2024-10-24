package com.lackofsky.cloud_s.service.data

import androidx.lifecycle.LiveData
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.model.Peer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.InetSocketAddress
import javax.inject.Inject

class SharedState @Inject constructor(
    private val userRepository: UserRepository
) {
    // Поток данных для обмена между компонентами

    private val _activeFriends = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val activeFriends: StateFlow<MutableSet<User>> = _activeFriends
    private val _activeStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())
    val activeStrangers: StateFlow<MutableSet<User>> = _activeStrangers
    /*** добавить флоу пиров (friends+strangers)
     *
     *  сервер -- принимает whoami - передает пользователя сюда. на onRemove мы его изымаем
     *
     *
     * активные - друзья,
     *            посторонние
     *
     * */
    var userOwner: LiveData<User>
    init{
        userOwner = userRepository.getUserOwner() //TODO( ISSUE:при смене данных о пользователе будут отправлятся изначальные данные  bad flow)
        userOwner.observeForever {  }
    }

    suspend fun addActiveUser(user: User){
        if(userRepository.getUserByUniqueID(user.uniqueID).isInitialized){
            userRepository.updateUser(user)
            _activeFriends.value.add(user)
        }else{
            _activeStrangers.value.add(user)
        }
    }

     fun removeActiveUser(peer: Peer) {
            _activeFriends.update { users ->
                users.removeIf { it.ipAddr == peer.address && it.port == peer.port }
                users
            }
            _activeStrangers.update { users ->
                users.removeIf { it.ipAddr == peer.address && it.port == peer.port }
                users
            }

    }
    fun addFriendInfo(userInfo: UserInfo){
        TODO()
    }

}