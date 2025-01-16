package com.lackofsky.cloud_s.ui.chats

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatDTO
import com.lackofsky.cloud_s.data.model.ChatListItem
import com.lackofsky.cloud_s.data.model.ChatType
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatsViewModel @Inject constructor(val userRepository: UserRepository,
                                         val chatRepository: ChatRepository, val messageRepository: MessageRepository
) :ViewModel() {
    private val _chats = MutableStateFlow<List<ChatListItem>?>(null)
    val chats: StateFlow<List<ChatListItem>?> get() = _chats

    init{
        viewModelScope.launch {
            chatRepository.getChatListItems().collect{
                _chats.value = it
            }
        }

//        chatRepository.getChatListItems().observeForever {
//            chatList->chats.value =  chatList
//        Log.d("GrimBerry 321 chatvm", chatList.toString())
//        }

    }
    //val chats = MutableLiveData<MutableSet<Chat>>(mutableSetOf())

    //val chatDtoList = MutableLiveData<MutableSet<ChatDTO>>(mutableSetOf())
//    init {
//        userRepository.getAllUsers()
////        for (i in 5..20) {
////
////            chats.value!!.add(Chat("chat " + i, "name " + i.toString(),ChatType.PRIVATE))
//////            val i = getLastMessageFrom("")
//////            i.value?.last()
////        }
//    }
    /***
     * Сделать навигацию к диалогу
     * */
}