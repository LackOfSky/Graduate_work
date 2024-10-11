package com.lackofsky.cloud_s.serviceP2P

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.lackofsky.cloud_s.serviceP2P.handlers.ProtocolHandler
import com.lackofsky.cloud_s.serviceP2P.model.Peer
import com.lackofsky.cloud_s.serviceP2P.netty.NettyServer

class P2PServer: Service() {
    private val _peersLiveData = MutableLiveData<Set<Peer>>()
    //val peersLiveData: LiveData<Set<Peer>> get() = _peersLiveData
    private val peers = mutableSetOf<Peer>()

    private lateinit var nettyServer: NettyServer
    private lateinit var peerDiscovery: PeerDiscovery
    private lateinit var protocolHandler: ProtocolHandler


    override fun onCreate() {
        super.onCreate()
        // Запускаем поиск пиров
        peerDiscovery = PeerDiscovery(
            onPeerDiscovered = { discoveredPeer ->
                peers.add(discoveredPeer)
                updatePeersLiveData()
                },
            onPeerRemoved = {removePeer ->
                peers.remove(removePeer)
                updatePeersLiveData()
                },
//            onPeerResolved = {resolvedPeer ->
//                peers.remove(removePeer)
//                updatePeersLiveData()}
        )
        peerDiscovery.startDiscovery()
        // Запускаем сервер для входящих подключений
        nettyServer = NettyServer(8080, peers) // порт для подключения
        nettyServer.start()
    }

    private fun updatePeersLiveData() {
        _peersLiveData.postValue(peers)
    }
//    private fun connectToPeer(peer: Peer) { //TODO вынести ближе к UI, отделить от сервиса
//        // Используем NettyClient для подключения к пиру
//        val nettyClient = NettyClient(peer.address, peer.port)
//        nettyClient.connect()
//    }

    override fun onDestroy() {
        super.onDestroy()
        peerDiscovery.stopDiscovery()
        nettyServer.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}