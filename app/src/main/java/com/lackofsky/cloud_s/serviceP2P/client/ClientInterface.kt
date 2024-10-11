package com.lackofsky.cloud_s.serviceP2P.client

import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.serviceP2P.P2PClient
import com.lackofsky.cloud_s.serviceP2P.model.Peer
import javax.inject.Inject

interface ClientInterface {
    fun connectToPeer(peer: Peer)
    fun sendMessage(clientName: String, message: Message):Boolean
}