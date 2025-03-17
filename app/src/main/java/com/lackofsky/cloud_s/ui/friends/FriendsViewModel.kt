package com.lackofsky.cloud_s.ui.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.usecase.StrangerRequestUseCase
import com.lackofsky.cloud_s.service.model.Peer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val clientPartP2P: ClientPartP2P,
    private val strangerRequestUseCase: StrangerRequestUseCase
//    private val clientServiceInterface: ClientInterface
) : ViewModel() {
      //val peers = MutableStateFlow<MutableSet<User>>(mutableSetOf()) //placeholder for strangers peers1

      val friends = userRepository.getAllUsers()
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
      val activeFriends = clientPartP2P.activeFriends.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
      val peers = clientPartP2P.activeStrangers
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
      val requestedStrangers = clientPartP2P.requestedStrangers
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
      val pendingStrangers = clientPartP2P.pendingStrangers
          .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tabTitlesList = listOf("Friends", "Add Friends")
    val tabTitlesItem = "Incoming requests"
    fun sendFriendRequest(stranger: User):Boolean{
        if(strangerRequestUseCase.sendFriendRequest(stranger)){
            clientPartP2P.addRequestedStranger(stranger)
            return true
        }else{
            return false
        }
    }
    fun cancelFriendRequest(stranger: User):Boolean{
        if(strangerRequestUseCase.cancelFriendRequest(stranger)){
            clientPartP2P.removeRequestedStranger(stranger)
            return true
        }else{
            return false
        }
    }
    fun rejectFriendRequest(stranger: User):Boolean{
        if(strangerRequestUseCase.rejectFriendRequest(stranger)){
            clientPartP2P.removePendingStranger(stranger)
            return true
        }else{
            return false
        }
    }
    fun approveFriendRequest(stranger: User):Boolean{
        Log.d("GrimBerry friends view model", "approveFriendRequest 1")
        Log.d("GrimBerry friends view model",clientPartP2P.activeStrangers.value.toString())
        Log.d("GrimBerry friends view model",clientPartP2P.pendingStrangers.value.toString())
        if(strangerRequestUseCase.approveFriendRequest(stranger)){
            CoroutineScope(Dispatchers.IO).launch{
                Log.d("GrimBerry friends view model", "approveFriendRequest 2")
                userRepository.insertUser(stranger)
                clientPartP2P.removePendingStranger(stranger)
                clientPartP2P.removeActiveUser(Peer(name ="", address = stranger.ipAddr))
                delay(1000)
                clientPartP2P.addActiveUser(stranger)

            }
            return true
        }else{
            Log.d("GrimBerry friends view model", "approveFriendRequest 0")
            return false
        }
    }
    fun isPeerInRequested(stranger: User):Boolean{
        return requestedStrangers.value.contains(stranger)
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
    suspend fun getPrivateChatId(userId: String):String{
        var chatId = ""
            chatId = chatRepository.getPrivateChatIdByUser(userId)!!
            Log.d("GrimBerry",chatId)

        return chatId
    }
}