package com.lackofsky.cloud_s.ui.friends

import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val clientPartP2P: ClientPartP2P
//    private val clientServiceInterface: ClientInterface
//    private val node: Near
) : ViewModel() {
//    val friends = node.friends//TODO (cделать\проверить логику)
      val peers1 = clientPartP2P.activeStrangers
      val peers = MutableStateFlow<MutableSet<User>>(mutableSetOf()) //placeholder for strangers peers1
      val friends = userRepository.getAllUsers()

      val requestedStrangers = MutableStateFlow<MutableSet<User>>(mutableSetOf())// в дальнейшем изменить на set

    init{
        val user = User(2,"Named nature","logic_A","fadfd213a2ek1")// не ставить id=1
        val user2 = User(3,"JohnDOe","logo","dsdsds33")// не ставить id=1
        val user3 = User(4,"Jeremy Sparks","kicked","ewewewe22")// не ставить id=1
        peers.value = mutableSetOf(user,user2,user3)
    }
    val tabTitlesList = listOf("Friends", "Add Friends", "Tab 3")
//    fun getCurrentUser(id:Int):StateFlow<HostUser>{
//        lateinit var hostUser: HostUser
//        for(it in friends.value){
//                if(it.user.id == id) hostUser = it
//        }
//        return MutableStateFlow<HostUser>(hostUser)
//    }
//
    fun sendFriendRequest(peer: User){
        requestedStrangers.value.add(peer)
    }
    fun cancelFriendRequest(peer: User){
        requestedStrangers.value.remove(peer)
    }


    fun addToFriends(peer: User){
        viewModelScope.launch {
            //node.
            userRepository.insertUser(peer)

        }
    }
    fun isPeerInRequested(peer: User):Boolean{
        return requestedStrangers.value.contains(peer)
    }
}