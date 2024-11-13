package com.lackofsky.cloud_s.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.repository.ChatRepository
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatDialogViewModel @Inject constructor(private val userRepository: UserRepository,
                                              private val messageRepository: MessageRepository,
                                              private val chatRepository: ChatRepository,
                                              private val clientPartP2P: ClientPartP2P
    ): ViewModel() {
    lateinit var messages: LiveData<List<Message>>

    init{

    }

    fun setMessagesList(chatId: String){
        CoroutineScope(Dispatchers.IO).launch {
            val chat = chatRepository.getChatById(chatId)
        }

        messages = messageRepository.getMessagesByChat(chatId)
    }

    fun sendMessage(text: String){
        //TODO("SEND message(text)")
    }

}