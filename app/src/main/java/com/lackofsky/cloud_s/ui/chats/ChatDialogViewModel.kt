package com.lackofsky.cloud_s.ui.chats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
    val messages = MutableLiveData<List<Message>>()
    val chatMembers = MutableLiveData<List<ChatMember>>()

    val activeFriends = MutableLiveData<List<User?>>()
    val activeChat = MutableLiveData<Chat>()
    val activeUserOwner = MutableLiveData<User>()

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
            messageRepository.getMessagesByChat(chatId).observeForever{
                    messageList -> messages.value = messageList
            }
//            chatMemberRepository.getMembersByChat(chatId).observeForever {
//                chatMembers.value = it
//            }
        userRepository.getUserOwner().observeForever {
            activeUserOwner.value = it
        }
        chatRepository.getChatById(chatId).observeForever {
            activeChat.value = it
            if(activeChat.value!!.type == ChatType.PRIVATE){
                activeFriends.value = mutableListOf(userRepository.getUserByUniqueID(chatId).value)
            } else{
                TODO("Реализация логики многопользовательского чата")
            }
        }
//            activeChat.value = chatRepository.getChatById(chatId).value//get info about current Chat
//
//                if(activeChat.value!!.type == ChatType.PRIVATE){
//                    activeFriends.value = mutableListOf(userRepository.getUserByUniqueID(chatId).value!!)
//                } else{
//                    TODO("Реализация логики многопользовательского чата")
//                }
    }

    fun sendMessage(text: String){
        //TODO("SEND message(text)")
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
                chatId = activeChat.value!!.chatId,
                content = text)

            if(chatMembers.value.isNullOrEmpty()){
                messageRepository.insertMessage(message)
            }else{
                try{
                    chatMembers.value?.forEach { member ->
                        //todo - фактически данная логика поставлена на то, что все пользователи онлайн
                        clientPartP2P.sendMessage(
                            userRepository.getUserByUniqueID(member.userId).value!!, message
                        )
                    }
                    messageRepository.insertMessage(message)
                }catch (e: Exception){
                    TODO(e.toString())
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