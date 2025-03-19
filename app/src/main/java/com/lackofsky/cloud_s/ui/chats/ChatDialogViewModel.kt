package com.lackofsky.cloud_s.ui.chats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatMember
import com.lackofsky.cloud_s.data.model.ChatType
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.database.repository.ChatMemberRepository
import com.lackofsky.cloud_s.data.database.repository.ChatRepository
import com.lackofsky.cloud_s.data.database.repository.MessageRepository
import com.lackofsky.cloud_s.data.database.repository.ReadMessageRepository
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDialogViewModel @Inject constructor(private val userRepository: UserRepository,
                                              private val messageRepository: MessageRepository,
                                              private val chatRepository: ChatRepository,
                                              private val chatMemberRepository: ChatMemberRepository,
                                              private val clientPartP2P: ClientPartP2P,
                                              //private val readMessageRepository: ReadMessageRepository
    ): ViewModel() {
    private val _messages = MutableStateFlow<List<Message>?>(null)
    val messages: StateFlow<List<Message>?> get() = _messages
    private val chatMembers = MutableStateFlow<List<ChatMember>?>(null)

    //val activeFriends = MutableStateFlow<List<User?>?>(null)
    private val _activeChat = MutableStateFlow<Chat?>(null)
    val activeUserOwner = MutableStateFlow<User?>(null)

    private val _selectedMessages = MutableStateFlow<MutableList<Message>>(mutableListOf())
    val selectedMessages: StateFlow<MutableList<Message>> = _selectedMessages
    private val _isSelectingMode = MutableStateFlow(_selectedMessages.value.isNotEmpty())
    val isSelectingMode: StateFlow<Boolean> = _isSelectingMode
    init{

    }

//    fun setMessagesList(chatId: String){
//        CoroutineScope(Dispatchers.IO).launch {
//            val chat = chatRepository.getChatById(chatId)
//        }
//
//        messages = messageRepository.getMessagesByChat(chatId)
//    }

    fun setChatId(chatId:String){
        viewModelScope.launch {
            messageRepository.getMessagesByChat(chatId).collect{
                _messages.value = it
            }
        }
        viewModelScope.launch {
            clientPartP2P.userOwner.collect{
                activeUserOwner.value = it
            }
        }
        viewModelScope.launch {
            chatRepository.getChatById(chatId).collect{
                _activeChat.value = it
                if (_activeChat.value!!.type == ChatType.PRIVATE){//TODO обработку ошибок
                    chatMemberRepository.getMembersByChat(chatId).collect{
                        chatMembers.value = it
                    }
                } else{
            //TODO("Реализация логики многопользовательского чата")
                }
            }
        }


//            chatMemberRepository.getMembersByChat(chatId).observeForever {
//                chatMembers.value = it
//            }


    }

    fun sendMessage(text: String){
        CoroutineScope(Dispatchers.IO).launch {
            var id = 0
            if(messages.value!!.isEmpty()){
                id = 1
            }else{
                id = messages.value!!.last().id+1
            }
            val message = Message(
                        id = id,
                        userId = activeUserOwner.value!!.uniqueID,
                        uniqueId = activeUserOwner.value!!.uniqueID + id,
                chatId = activeUserOwner.value!!.uniqueID,// у власника чат айді = _activeChat.value!!.chatId, у другої сторони = айді власника
                content = text)

            val ownChatId = _activeChat.value!!.chatId
            val messageToSave = message.copy(chatId = ownChatId)


            if(chatMembers.value.isNullOrEmpty()){
                Log.d("GrimBerry chatDialogVM", "chatMembers.value.isNullOrEmpty()")
                messageRepository.insertMessage(message)
            }else{
                try{
                    chatMembers.value?.forEach { member ->
                        //todo - фактически данная логика поставлена на то, что все пользователи онлайн
                        Log.d("GrimBerry chatDialogVM", message.toString())
                        clientPartP2P.sendMessage(
                            activeFriend = userRepository.getUserByUniqueID(member.userId).first(),
                            message = message
                        )
                        Log.d("GrimBerry chatDialogVM", "send message")
                    }

                    messageRepository.insertMessage( messageToSave )
                }catch (e: Exception){
                    //додати вспливаюче повідомлення що користувач не онлайн
                    Log.d("GrimBerry chatDialogVM", e.toString())
                }
            }

        }
    }

    fun deleteMessage(message: Message):Boolean{
        //фича - удаление сообщений будет производится лишь у себя
        CoroutineScope(Dispatchers.IO).launch{
            messageRepository.deleteMessage(message)
        }
        return true
    }
    fun copyToClipboard(context: Context, text: String):Boolean {
        // Получаем ClipboardManager
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Создаём ClipData с текстом
        val clip = ClipData.newPlainText("Copied Text", text)

        // Копируем текст в буфер обмена
        clipboard.setPrimaryClip(clip)

        // Уведомляем пользователя о копировании
        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        return true
    }

    fun selectedMessage(message: Message,isSelected:Boolean){
        if(isSelected){
            selectedMessages.value.add(message)
            _isSelectingMode.value = true //Потенциально не имеет смысла каждый раз дёргать #todo
        }else{
            selectedMessages.value.remove(message)
            if(selectedMessages.value.isEmpty()){
                _isSelectingMode.value = false
            }
        }
    }
    fun isFromOwner(userId: String):Boolean{
        return activeUserOwner.value!!.uniqueID == userId

    }
}