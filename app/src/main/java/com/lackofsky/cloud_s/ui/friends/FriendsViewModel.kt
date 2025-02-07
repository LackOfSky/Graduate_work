package com.lackofsky.cloud_s.ui.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.usecase.StrangerRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
      val peers = clientPartP2P.activeStrangers
      val requestedStrangers = clientPartP2P.requestedStrangers// в дальнейшем изменить на set
      val pendingStrangers = clientPartP2P.pendingStrangers

    val tabTitlesList = listOf("Friends", "Add Friends")
    val tabTitlesItem = "Incoming requests"
    fun sendFriendRequest(stranger: User):Boolean{
        peers.let { item -> Log.d("GrimBerry test",item.value.keys.contains(stranger).toString() )}
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
//        Log.d("GrimBerry test",peers.value.toString() )
//        Log.d("GrimBerry test",stranger.toString() )
        val test =peers.value.get(stranger)
        if(strangerRequestUseCase.rejectFriendRequest(stranger)){
            Log.d("GrimBerry", "rejectFriendRequest step 2 ")
            clientPartP2P.removePendingStranger(stranger)
            return true
        }else{
            return false
        }
    }
    fun approveFriendRequest(stranger: User):Boolean{
        if(strangerRequestUseCase.approveFriendRequest(stranger)){
            Log.d("GrimBerry", "approveFriendRequest step 2")
            CoroutineScope(Dispatchers.IO).launch{
                userRepository.insertUser(stranger)
                clientPartP2P.removePendingStranger(stranger)
            }
            return true
        }else{
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