package com.lackofsky.cloud_s.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val clientPartP2P: ClientPartP2P,

//    private val clientServiceInterface: ClientInterface
) : ViewModel() {

    fun getFriend(friendID: Int): Flow<User> {
        return userRepository.getUserById(friendID)
    }
    fun getFriendInfo(friendID: String): Flow<UserInfo>{
        return userRepository.getUserInfoById(friendID)
    }
}