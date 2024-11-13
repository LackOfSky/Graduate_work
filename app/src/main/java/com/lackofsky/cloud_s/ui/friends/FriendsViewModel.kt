package com.lackofsky.cloud_s.ui.friends

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.ChatRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.usecase.FriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val clientPartP2P: ClientPartP2P,
    private val friendRequestUseCase: FriendRequestUseCase
//    private val clientServiceInterface: ClientInterface
) : ViewModel() {
      //val peers = MutableStateFlow<MutableSet<User>>(mutableSetOf()) //placeholder for strangers peers1

      val friends = userRepository.getAllUsers()
      val peers = clientPartP2P.activeStrangers
      val requestedStrangers = clientPartP2P.requestedStrangers// в дальнейшем изменить на set
      val pendingStrangers = clientPartP2P.pendingStrangers
    init{
        val user = User(2,"Named nature","logic_A","fadfd213a2ek1")// не ставить id=1
        val user2 = User(3,"JohnDOe","logo","dsdsds33")// не ставить id=1
        val user3 = User(4,"Jeremy Sparks","kicked","ewewewe22")// не ставить id=1
        for (i in 5..20) {

            //peers.value.add(User(i,"name "+i.toString(),"login "+i.toString(),"unique id "+ i.toString()))
        }
        //peers.value = MutableSetOf(user,user2,user3)//placeholder for strangers peers1
    }
    val tabTitlesList = listOf("Friends", "Add Friends")
    val tabTitlesItem = "Incoming requests"
    fun sendFriendRequest(peer: User):Boolean{
        if(friendRequestUseCase.sendFriendRequest(peer)){
            clientPartP2P.addRequestedStranger(peer)
            return true
        }else{
            return false
        }
    }
    fun cancelFriendRequest(peer: User):Boolean{
        if(friendRequestUseCase.cancelFriendRequest(peer)){
            clientPartP2P.removeRequestedStranger(peer)
            return true
        }else{
            return false
        }
    }
    fun rejectFriendRequest(peer: User):Boolean{
        if(friendRequestUseCase.rejectFriendRequest(peer)){
            clientPartP2P.removePendingStranger(peer)
            return true
        }else{
            return false
        }
    }
    fun approveFriendRequest(peer: User):Boolean{
        if(friendRequestUseCase.approveFriendRequest(peer)){
            CoroutineScope(Dispatchers.IO).launch{
                userRepository.insertUser(peer)
                chatRepository.createPrivateChat(userRepository.getUserOwner().value!!.uniqueID,peer.uniqueID) //mirror for serverHandler
                clientPartP2P.removePendingStranger(peer)
            }
            return true
        }else{
            return false
        }
    }
    fun isPeerInRequested(peer: User):Boolean{
        return requestedStrangers.value.contains(peer)
    }
    fun editFriendName(friend: User):Boolean{
        try {
            CoroutineScope(Dispatchers.IO).launch {
        userRepository.updateUser(friend)
            }
        }catch (e: Exception){
            Log.d("GrimBerry friends view model", e.toString())
            return false
        }
        return true
    }
    fun deleteFriend(friend: User):Boolean{
        try {
            CoroutineScope(Dispatchers.IO).launch {
                userRepository.deleteUser(friend)
            }
        }catch (e: Exception){
            Log.d("GrimBerry friends view model", e.toString())
            return false
        }
        return true
    }

}