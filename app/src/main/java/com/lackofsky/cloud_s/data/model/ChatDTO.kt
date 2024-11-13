package com.lackofsky.cloud_s.data.model

import androidx.lifecycle.LiveData

data class ChatDTO(val friend: User,
                   val messages: LiveData<List<Message>>)