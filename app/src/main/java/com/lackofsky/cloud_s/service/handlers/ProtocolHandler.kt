package com.lackofsky.cloud_s.service.handlers

import com.lackofsky.cloud_s.service.model.Peer


class ProtocolHandler {
    enum class Protocol {
         NOISE_TCP, NOISE_UDP
    }

    fun chooseProtocol(peer: Peer): Protocol {
        return when {
            peer.port == 12345 -> Protocol.NOISE_TCP
            peer.name.contains("udp") -> Protocol.NOISE_UDP
            else -> Protocol.NOISE_TCP
        }
    }

    fun connect(peer: Peer) {
        val protocol = chooseProtocol(peer)
        when (protocol) {
            Protocol.NOISE_TCP -> connectViaNoiseTcp(peer)
            Protocol.NOISE_UDP -> connectViaNoiseUdp(peer)
        }
    }
    private fun connectViaNoiseTcp(peer: Peer) {
        // Логика подключения с использованием Noise поверх TCP
    }

    private fun connectViaNoiseUdp(peer: Peer) {
        // Логика подключения с использованием Noise поверх UDP
    }

}