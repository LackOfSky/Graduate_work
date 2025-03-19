package com.lackofsky.cloud_s.ui.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.P2PServer.Companion.SERVICE_NAME
import com.lackofsky.cloud_s.service.client.NettyClient
import com.lackofsky.cloud_s.service.client.usecase.FriendRequestUseCase
import com.lackofsky.cloud_s.service.client.usecase.StrangerRequestUseCase
import com.lackofsky.cloud_s.service.model.Peer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val clientPartP2P: ClientPartP2P,
    private val strangerRequestUseCase: StrangerRequestUseCase,
    private val friendRequestUseCase: FriendRequestUseCase
//    private val clientServiceInterface: ClientInterface
) : ViewModel() {
      //val peers = MutableStateFlow<MutableSet<User>>(mutableSetOf()) //placeholder for strangers peers1
      init {

          viewModelScope.launch {
//              clientPartP2P.activeStrangers.collect { activeStrangers ->
////                  _peers.update { activeStrangers.keys.toMutableSet() }
////              }
//                  activeStrangers?.let { map ->
//                      _peers.update { map.keys.toMutableSet() }
//                  } ?: run {
//                      _peers.update { emptySet() }
//                  }
//              }
          }

          viewModelScope.launch {
              userRepository.getAllUsers()
                  .combine(clientPartP2P.activeFriends) { allUsers, activeFriends ->
                      val (online, offline) = allUsers.partition { user ->
                          Log.d("service GrimBerry :client", "Active friends emitted: ${user.toString()}")
                          activeFriends.keys.any{activeUser->activeUser.uniqueID == user.uniqueID}
                      }
                      Pair(online, offline)
                  }
                  .collect { (online, offline) ->
                      Log.d("service GrimBerry :client", "add active friend")
                      _friendsOnline.value = online
                      _friendsOffline.value = offline
                      Log.d("service GrimBerry :client", _friendsOnline.value.toString())
                  }
          }
      }

    private val _friendsOnline = MutableStateFlow(emptyList<User>())
    val friendsOnline : StateFlow<List<User>> get() = _friendsOnline

    private val _friendsOffline = MutableStateFlow(emptyList<User>())
    val friendsOffline : StateFlow<List<User>> get() = _friendsOffline

    val peers: StateFlow<Set<User>> = clientPartP2P.activeStrangers.map { activeFriendsMap ->
        activeFriendsMap.keys.toSet()
    }.stateIn(scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    val requestedStrangers : StateFlow<Set<User>> = clientPartP2P.requestedStrangers
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
                clientPartP2P.addStrangerToFriend(stranger.uniqueID)
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
    fun deleteFriend(friend: User, forAll: Boolean = false):Boolean{
        try {
            CoroutineScope(Dispatchers.IO).launch {
            if(forAll){
                val client = clientPartP2P.activeFriends.value.entries
                    .firstOrNull { it.key.uniqueID == friend.uniqueID }?.value

                Log.d("GrimBerry friends view model", "client "+ client.toString())
                if(friendRequestUseCase.deleteFriendRequest(client!!)){
                    Log.d("GrimBerry friends view model", "deleteFriend: success")
                    userRepository.deleteUser(friend)
                    clientPartP2P.deleteFriendToStranger(friend.uniqueID)
                }
            }else{
                userRepository.deleteUser(friend)
            }

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