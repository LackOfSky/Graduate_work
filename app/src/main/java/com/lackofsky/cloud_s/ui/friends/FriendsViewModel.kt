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
        if(strangerRequestUseCase.approveFriendRequest(stranger)){
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