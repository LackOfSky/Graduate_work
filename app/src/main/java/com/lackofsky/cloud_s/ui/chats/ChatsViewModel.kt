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
import com.lackofsky.cloud_s.service.ClientPartP2P
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ChatsViewModel @Inject constructor(val userRepository: UserRepository,
                                         val chatRepository: ChatRepository,
                                         val messageRepository: MessageRepository,
    val clientPartP2P: ClientPartP2P
) :ViewModel() {
//    val chats: StateFlow<List<ChatListItem>?> = chatRepository.getChatListItems()
//        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

//    val chats: StateFlow<Map<ChatListItem, Boolean>> = chatRepository.getChatListItems().map { items->
//        items.associateWith {item ->
//            clientPartP2P.activeFriends.value.keys.any { it.uniqueID == item.chatName }
//        }

//    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap() )
val chats: StateFlow<Map<ChatListItem, Boolean>> = chatRepository.getChatListItems()
        .combine(clientPartP2P.activeFriends) { chatItems, activeFriends ->
            chatItems.associateWith { item ->
                activeFriends.keys.any { it.uniqueID == item.chatName }
            }
        }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )
val lastNoteMessage: StateFlow<Message?> = messageRepository.getLastNoteMessage().stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = null )
val userOwner: StateFlow<User?> = clientPartP2P.userOwner
    .stateIn(scope = viewModelScope, SharingStarted.Eagerly,
    initialValue = null )
}