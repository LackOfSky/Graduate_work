package com.lackofsky.cloud_s.ui.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
//    private val node: Near
) : ViewModel() {
//    val friends = node.friends//TODO (cделать\проверить логику)
//    val strangers = node.strangers

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