package com.lackofsky.cloud_s.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.services.p2pService.P2pByNear
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val node: P2pByNear
) : ViewModel() {
    val users = node.hosts


    //под снос
    var _currentUser = MutableStateFlow<User>(
        User(1,"John Doe", //TODO подхват с БД
            "@just_someone",
            "     Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
                    "sed do eiusmod tempor incididunt " +
                    "ut labore et dolore magna aliqua incididunt ut labore et dolore magna aliqua. \n",

            "     Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
                    "eiusmod tempor incididunt ut labore et dolore magna aliqua incididunt ut " +
                    "labore et dolore magna aliqua. \n",
        )
    )
    val currentUser : StateFlow<User> = _currentUser

    val tabTitlesList = listOf("Friends", "Add Friends", "Tab 3")

    fun getFriendList():List<User>{
        val friendList = listOf(currentUser.value,currentUser.value,currentUser.value,
            currentUser.value,currentUser.value,currentUser.value,
            currentUser.value,currentUser.value,currentUser.value)
        return friendList
    }

    fun setCurrentFriend(userId:Int){
        // return userService.getUserById(userId:Int)
        _currentUser = MutableStateFlow<User>(currentUser.value)//todo ccылаться на нового юзера
    }

    fun getNods(){
//        node.
    }
}