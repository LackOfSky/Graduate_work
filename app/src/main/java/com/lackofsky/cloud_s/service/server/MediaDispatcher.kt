package com.lackofsky.cloud_s.service.server


import android.provider.MediaStore
import com.lackofsky.cloud_s.service.netty_media_p2p.model.TransferMediaIntend
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

class MediaDispatcher {
    private val activeTransfers = mutableSetOf<String>() // Поточні передачі
    private val queue: Queue<String> = ConcurrentLinkedQueue() // Черга очікування

    private val maxConcurrentTransfers = 10 // Наприклад, максимум 10 передач одночасно
    private var _mediaServerAddress :String? = null
    val mediaServerAddress = _mediaServerAddress
    private var _mediaServerPort :Int? = null
    val mediaServerPort = _mediaServerPort
    @Synchronized
    fun requestTransfer(peerId: String): Boolean {
        return if (activeTransfers.size < maxConcurrentTransfers) {
            activeTransfers.add(peerId)
            true
        } else {
            queue.add(peerId)
            false
        }
    }

    @Synchronized
    fun completeTransfer(peerId: String) {
        activeTransfers.remove(peerId)
        queue.poll()?.let { nextPeer ->
            activeTransfers.add(nextPeer)
            // Тут можна відправити команду медіасерверу почати нову передачу
        }
    }

    fun setMediaServerAddress(ipAddress: String, port: Int) {
        _mediaServerAddress = ipAddress
        _mediaServerPort = port
    }
}

enum class MediaResponseStatus { QUEUETED, ACCEPTED, REJECTED }

data class MediaResponse(val status: MediaResponseStatus,
                         val msIpAddress: String?,
                         val msPort: Int?,
                         val requestedIntend: TransferMediaIntend?,
                         val messageId: String?/*** if intend = MEDIA_EXTERNAL  (message media)*/
)
data class MediaRequest(
                       val requestedIntend: TransferMediaIntend,
                       val messageId: String?/*** if intend = MEDIA_EXTERNAL  (message media)*/
)