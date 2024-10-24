package com.lackofsky.cloud_s.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.data.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedState: SharedState
//    private val clientServiceInterface: ClientInterface
//    private val node: Near
) : ViewModel() {
//    val friends = node.friends//TODO (cделать\проверить логику)
    val peers = sharedState.activeStrangers

    val tabTitlesList = listOf("Friends", "Add Friends", "Tab 3")
//    fun getCurrentUser(id:Int):StateFlow<HostUser>{
//        lateinit var hostUser: HostUser
//        for(it in friends.value){
//                if(it.user.id == id) hostUser = it
//        }
//        return MutableStateFlow<HostUser>(hostUser)
//    }
//
    fun addToFriends(peer: User){
        viewModelScope.launch {
            //node.
            userRepository.insertUser(peer)

        }
    }
}