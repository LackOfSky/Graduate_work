package com.lackofsky.cloud_s.ui.chats

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
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
import com.lackofsky.cloud_s.data.model.MessageContentType
import com.lackofsky.cloud_s.data.storage.StorageRepository
import com.lackofsky.cloud_s.data.storage.UserInfoStorageFolder
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.usecase.MessageRequestUseCase
import com.lackofsky.cloud_s.service.model.MessageKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDialogViewModel @Inject constructor(private val userRepository: UserRepository,
                                              private val messageRepository: MessageRepository,
                                              private val chatRepository: ChatRepository,
                                              private val chatMemberRepository: ChatMemberRepository,
                                              private val clientPartP2P: ClientPartP2P,
                                              private val storageRepository: StorageRepository,
    private val messageRequestUseCase: MessageRequestUseCase
                                              //private val readMessageRepository: ReadMessageRepository
    ): ViewModel() {
    private val _messages = MutableStateFlow<List<Message>?>(null)
    val messages: StateFlow<List<Message>?> get() = _messages
    private val chatMembers = MutableStateFlow<List<ChatMember>>(emptyList())

    //val activeFriends = MutableStateFlow<List<User?>?>(null)
    private val _activeChat = MutableStateFlow<Chat?>(null)
    val activeChat: StateFlow<Chat?> get() = _activeChat
    val activeUserOwner = MutableStateFlow<User?>(null)
    val activeUserOne2One = userRepository.getAllUsers().map { friends->
        friends.find { user ->
            user.uniqueID == _activeChat.value?.name && _activeChat.value?.type == ChatType.PRIVATE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )
    val isNotesChat: StateFlow<Boolean> = activeChat.combine(activeUserOwner){chat, owner ->
        chat?.chatId.orEmpty() == owner?.uniqueID.orEmpty()
    }.stateIn(scope = viewModelScope,started = SharingStarted.Lazily, initialValue = false)

    private val _selectedMessages = MutableStateFlow<MutableList<Message>>(mutableListOf())
    val selectedMessages: StateFlow<MutableList<Message>> = _selectedMessages
    private val _isSelectingMode = MutableStateFlow(_selectedMessages.value.isNotEmpty())
    val isSelectingMode: StateFlow<Boolean> = _isSelectingMode

    //private val _isFriendOnline = MutableStateFlow(false)//= clientPartP2P.friendsOnline.map { friends -> friends.any { it.uniqueID == _activeChat.value!!.name } }
    val isFriendOnline: StateFlow<Boolean> = clientPartP2P.friendsOnline.map { //get()
        friends -> friends.any{
            it.uniqueID == _activeChat.value?.name && _activeChat.value?.type == ChatType.PRIVATE}
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)//_isFriendOnline


    fun setChatId(chatId:String){
        viewModelScope.launch {
            messageRepository.getMessagesByChat(chatId).collect{
                _messages.value = it
                Log.d("GrimBerry chatDialogVM", "setChatId: ${it.toString()}")
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
    }

    fun sendMessage(context: Context,text: String){//todo change to sendPrivateMessage
        CoroutineScope(Dispatchers.IO).launch {

            val ownId = activeUserOwner.value!!.uniqueID

            var messageToSave = Message(
                userId = activeUserOwner.value!!.uniqueID,
                chatId = _activeChat.value!!.chatId ,// у власника чат айді = _activeChat.value!!.chatId, у другої сторони = айді власника
                replyMessageId = attachedMessageReply.value?.uniqueId,
                content = text)
            attachReply(null)
            val uriItem = _uriItem.value
            uriItem?.let{uri ->
                    messageToSave = saveMessageFile(context, uri, uri.lastPathSegment!!, messageToSave)
                    Log.d("GrimBerry chatDialogVM", "sendMessage: ${messageToSave.toString()}")
                    attachMedia(null)
                }


            Log.d("GrimBerry chatDialogVM", "chatMembers.value ${chatMembers.value.toString()} ")
            if(chatMembers.value.size == 1){
                messageRepository.insertAndUpdateMessage(messageToSave)
            }else{
                try{
                    val activeChat = _activeChat.value!!
                    val messageToSend = messageRepository.insertAndUpdateMessage( messageToSave )
                        .copy(chatId = ownId)
                    chatMembers.value.forEach { member ->
                        //todo - фактически данная логика поставлена на то, что все пользователи онлайн
                        // upd - наразі ця логіка для використання для one2one
                        if(member.userId != activeUserOwner.value!!.uniqueID){
                            val client = clientPartP2P.activeFriends.value.entries
                                .firstOrNull {
                                    it.key.uniqueID == activeChat.name && activeChat.type == ChatType.PRIVATE
                                }?.value!!
                            messageRequestUseCase.sendMessageRequest(client, messageToSend)
                            uriItem?.let { uri ->
                                messageRequestUseCase.sendMediaMessageRequest(sendTo = client, messageToSend)
                            }

                            Log.d("GrimBerry chatDialogVM", "send message")
                        }
                    }

                }catch (e: Exception){
                    //додати вспливаюче повідомлення що користувач не онлайн
                    Log.d("GrimBerry chatDialogVM", e.toString())
                }
            }

        }
    }
    private fun saveMessageFile(context: Context, uri: Uri, fileName: String, messageToSave: Message): Message {
        val mimeType = context.contentResolver.getType(uri) ?: throw Exception("chatDialogViewModel saveMessageFile. cannot get mimeType")

        return when {
            mimeType.startsWith("image/") -> {
                messageToSave.copy(
                    mediaUri =  storageRepository.saveImageToGallery(context, uri, fileName).toString(),
                    contentType = MessageContentType.IMAGE
                )
            }
            mimeType.startsWith("video/") -> {
                messageToSave.copy(
                    mediaUri =  storageRepository.saveVideoToGallery(context, uri, fileName).toString(),
                    contentType = MessageContentType.VIDEO
                )

            }
            mimeType.startsWith("audio/") -> {
                messageToSave.copy(
                    mediaUri =  storageRepository.saveAudioToMusic(context, uri, fileName).toString(),
                    contentType = MessageContentType.AUDIO
                )

            }
            mimeType == "application/pdf" || mimeType.startsWith("text/") ||
                    mimeType == "application/msword" || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                    mimeType == "application/vnd.ms-excel" || mimeType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> {

                messageToSave.copy(
                    mediaUri =  storageRepository.saveDocumentToDocuments(context, uri, fileName).toString(),
                    contentType = MessageContentType.DOCUMENT
                )
            }
            else -> {
                messageToSave.copy(
                    mediaUri =  storageRepository.saveFileToDownloads(context, uri, fileName).toString(),
                    contentType = MessageContentType.DOCUMENT
                )

            }
        }
    }

    private val _uriItem = MutableStateFlow<Uri?>(null)
    val uriItem: StateFlow<Uri?> = _uriItem
    private val _isMediaAttached = MutableStateFlow(false) //.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isMediaAttached: StateFlow<Boolean> = _isMediaAttached

    private val _attachedMessageReply = MutableStateFlow<Message?>(null)
    val attachedMessageReply: StateFlow<Message?> = _attachedMessageReply
    private val _isReplyAttached = MutableStateFlow(false)
    val isReplyAttached: StateFlow<Boolean> = _isReplyAttached
    fun attachReply(message: Message?){
        _attachedMessageReply.update { message }
        _isReplyAttached.value = (_attachedMessageReply.value != null)
    }
    fun attachMedia(uriItem: Uri?){//todo for list Uri
        _uriItem.update { uriItem }
        _isMediaAttached.value =  (uriItem != null)
    }
    fun deleteNotedMessage(message: Message){
        CoroutineScope(Dispatchers.IO).launch {
            messageRepository.deleteMessage(message)
        }
    }
    fun updateMessage(message: Message){}
    fun deleteMessage(message: Message, forOneToOne: Boolean = true, forMyself: Boolean = false):Boolean{
        //фича - удаление сообщений будет производится лишь у себя
        try {
            CoroutineScope(Dispatchers.IO).launch {
                if(forMyself){
                    messageRepository.deleteMessage(message)
                    return@launch
                }
                if(forOneToOne){
                    //clientPartP2P.activeFriends.value
                    val activeChat = _activeChat.value!!
                    val client = clientPartP2P.activeFriends.value.entries
                        .firstOrNull {
                            it.key.uniqueID == activeChat.name && activeChat.type == ChatType.PRIVATE
                        }?.value!!

                    val isSuccess = messageRequestUseCase.deleteMessageOne2OneRequest(client, message.uniqueId!!)
                    if(isSuccess){
                        messageRepository.deleteMessage(message)
                        Log.d("GrimBerry chat dialog viewModel", "deleteMessage: success")
                    }else{
                        Log.d("GrimBerry chat dialog viewModel", "deleteMessage: failed")
                    }

                }else{
                    messageRepository.deleteMessage(message)
                }

            }
        }catch (e: Exception){
            Log.d("GrimBerry friends view model", e.toString())
            return false
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
    fun openDocumentWithIntent(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, context.contentResolver.getType(uri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Open file with...")
        try {
            context.startActivity(chooser)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Seems you don`t have application to open this file", Toast.LENGTH_SHORT).show()
        }
    }
    fun getAudioMetadata(context: Context, uri: Uri): Pair<String?, String?> {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)

            // Получаем название и исполнителя
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            return title to artist
        } catch (e: Exception) {
            e.printStackTrace()
            return null to null
        } finally {
            retriever.release()
        }
    }

    fun getMessage(messageId: String): Flow<Message> {
        return messageRepository.getMessageByUniqueId(messageId)
    }


    private val _animateScrollID = MutableStateFlow<String?>(null)
    val animateScrollID: StateFlow<String?> = _animateScrollID
    fun findMessageScrollingIndex(messageUniqueId: String):Int? {
        return messages.value?.indexOfFirst { it.uniqueId == messageUniqueId}

    }
    fun setAnimateScrollingID(messageUniqueId: String?){
        _animateScrollID.update { messageUniqueId }
    }
}