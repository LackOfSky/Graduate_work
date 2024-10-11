package com.lackofsky.cloud_s.serviceP2P

import com.lackofsky.cloud_s.serviceP2P.model.Peer
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

class PeerDiscovery(private val onPeerDiscovered: (Peer) -> Unit,
                    //private val onPeerResolved: (Peer) -> Unit,
                    private val onPeerRemoved: (Peer) -> Unit) {
    private lateinit var jmdns: JmDNS

    fun startDiscovery() {
        jmdns = JmDNS.create(InetAddress.getLocalHost())
        jmdns.addServiceListener("_p2p._tcp.local.", object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                val peer = Peer(event.name, event.info.hostAddresses.first(), event.info.port)
                onPeerDiscovered(peer)
            }

            override fun serviceRemoved(event: ServiceEvent) {
                // Обработка удаления пира
                val peer = Peer(event.name, event.info.hostAddresses.first(), event.info.port)
                onPeerRemoved(peer)
            }

            override fun serviceResolved(event: ServiceEvent) {
                // Обработка разрешения пира
                val peer = Peer(event.name, event.info.hostAddresses.first(), event.info.port)
                //onPeerResolved(peer)
            }
        })
    }

    fun stopDiscovery() {
        jmdns.unregisterAllServices()
        jmdns.close()
    }
}