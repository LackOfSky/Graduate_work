package com.lackofsky.cloud_s.ui.friends

import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.data.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sharedState: SharedState
//    private val clientServiceInterface: ClientInterface
//    private val node: Near
) : ViewModel() {
//    val friends = node.friends//TODO (cделать\проверить логику)
    val peers = sharedState.peersFlow

    val tabTitlesList = listOf("Friends", "Add Friends", "Tab 3")
//    fun getCurrentUser(id:Int):StateFlow<HostUser>{
//        lateinit var hostUser: HostUser
//        for(it in friends.value){
//                if(it.user.id == id) hostUser = it
//        }
//        return MutableStateFlow<HostUser>(hostUser)
//    }
//
//    fun addToFriends(hostUser: HostUser){
//        viewModelScope.launch {
//            //node.
//            userRepository.insertUser(hostUser.user)
//
//        }
//    }
}