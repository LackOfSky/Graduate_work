package com.lackofsky.cloud_s.service.netty_media_p2p.handlers

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class FileTransferDecoder : ByteToMessageDecoder() {

    private var state = State.READ_LENGTH
    private var metadataLength = 0
    private var chunkId = 0
    private enum class State {
        READ_LENGTH,
        READ_METADATA,
        READ_HEADER,
        READ_FILE,
        READ_COMPLETE
    }

    private lateinit var metadata: JSONObject
    private lateinit var fileBuffer: ByteArrayOutputStream
    private var fileBytesRemaining = 0

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        when (state) {
            State.READ_LENGTH -> {
                if (`in`.readableBytes() < 4) return
                metadataLength = `in`.readInt()
                state = State.READ_METADATA
            }

            State.READ_METADATA -> {
                if (`in`.readableBytes() < metadataLength) return
                val metadataBytes = ByteArray(metadataLength)
                `in`.readBytes(metadataBytes)
                metadata = JSONObject(String(metadataBytes))
                fileBytesRemaining = metadata.getInt("size")//  (buildChunkHeader ?)
                fileBuffer = ByteArrayOutputStream()
                state = State.READ_FILE
            }
            State.READ_HEADER -> {
                // Чтение заголовка чанка (JSON с chunkId и chunkSize)
                if (`in`.readableBytes() < 2) return  // Ожидаем хотя бы 2 байта для минимального заголовка

                val chunkHeaderSize = 256  // Примерный размер заголовка
                if (`in`.readableBytes() < chunkHeaderSize) return

                val headerBytes = ByteArray(chunkHeaderSize)
                `in`.readBytes(headerBytes)
                val headerJson = String(headerBytes, Charsets.UTF_8)
                val header = JSONObject(headerJson)

                chunkId = header.getInt("chunkId")
                val chunkSize = header.getInt("chunkSize")

                if (chunkSize <= 0) return

                state = State.READ_FILE
            }
            State.READ_FILE -> {
                val chunkSize = minOf(`in`.readableBytes(), fileBytesRemaining)
                if (chunkSize <= 0) return
                val fileChunk = ByteArray(chunkSize)
                `in`.readBytes(fileChunk)
                fileBuffer.write(fileChunk)
                fileBytesRemaining -= chunkSize

                if (fileBytesRemaining == 0) {
                    out.add(ReceivedFile(metadata, fileBuffer.toByteArray()))
                    state = State.READ_COMPLETE
                }
            }

            State.READ_COMPLETE -> {
                val line = `in`.readBytes(`in`.readableBytes()).toString(Charsets.UTF_8)
                if (line.contains("FILE_COMPLETE")) {
                    println("Файл повністю отримано!")
                    state = State.READ_LENGTH
                }
            }
        }
    }
}

data class ReceivedFile(val metadata: JSONObject, val data: ByteArray)