package com.lackofsky.cloud_s.service.model

import java.net.InetAddress

data class Peer(
    val name: String,
    val address: String
)
sealed class PeerConnectionStatus {
    data class Connected(val peer: Peer) : PeerConnectionStatus()
    data class Disconnected(val peer: Peer) : PeerConnectionStatus()
    data class Failed(val peer: Peer, val reason: String) : PeerConnectionStatus()
}