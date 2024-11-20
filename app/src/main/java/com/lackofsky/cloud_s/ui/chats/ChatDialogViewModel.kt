package com.lackofsky.cloud_s.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lackofsky.cloud_s.data.model.Chat
import com.lackofsky.cloud_s.data.model.ChatMember
import com.lackofsky.cloud_s.data.model.ChatType
import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.ChatMemberRepository
import com.lackofsky.cloud_s.data.repository.ChatRepository
import com.lackofsky.cloud_s.data.repository.MessageRepository
import com.lackofsky.cloud_s.data.repository.UserRepository
import com.lackofsky.cloud_s.service.ClientPartP2P
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDialogViewModel @Inject constructor(private val userRepository: UserRepository,
                                              private val messageRepository: MessageRepository,
                                              private val chatRepository: ChatRepository,
                                              private val chatMemberRepository:ChatMemberRepository,
                                              private val clientPartP2P: ClientPartP2P
    ): ViewModel() {
    val messages = MutableLiveData<List<Message>>()
    val chatMembers = MutableLiveData<List<ChatMember>>()
    val activeFriends = MutableLiveData<List<User?>>()
    val activeChat = MutableLiveData<Chat>()
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    val activeUserOwner = MutableLiveData<User>()
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
//        activeUserOwner.value = userRepository.getUserOwner().value



//        coroutineScope.launch {
            //get info about app Owner


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
//            }






    }
    fun sendMessage(text: String){
        //TODO("SEND message(text)")
        CoroutineScope(Dispatchers.IO).launch {
            val id = messageRepository.getMessagesCount(activeChat.value!!.chatId).toInt()+1
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
    fun selectedMessage(message: Message){

    }
}