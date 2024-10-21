package com.lackofsky.cloud_s.service.client

import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.service.model.Peer

interface ClientInterface {
    fun connectToPeer(peer: Peer)
    fun sendMessage(clientName: String, message: Message):Boolean
}