package com.lackofsky.cloud_s.service.client

import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.service.model.Peer

interface ClientInterface {
    fun connectTo(activeFriend: User)
    fun disconnect(activeFriend: User)
    fun sendMessage(activeFriend: User, message: Message):Boolean
}