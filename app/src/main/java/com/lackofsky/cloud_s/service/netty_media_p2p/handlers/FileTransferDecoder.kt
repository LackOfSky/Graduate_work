package com.lackofsky.cloud_s.service.netty_media_p2p.handlers

import com.google.gson.Gson
import com.lackofsky.cloud_s.service.netty_media_p2p.model.MediaRequest
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class FileTransferDecoder(val gson: Gson) : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        if (`in`.readableBytes() < 5) return // Тип (1) + длина (4)

        `in`.markReaderIndex()

        val type = `in`.readByte()
        val length = `in`.readInt()

        if (`in`.readableBytes() < length) {
            `in`.resetReaderIndex()
            return
        }

        val content = ByteArray(length)
        `in`.readBytes(content)

        when (type) {
            0x01.toByte() -> { // JSON
                val json = String(content, Charsets.UTF_8)
                val request = gson.fromJson(json, MediaRequest::class.java)
                out.add(request)
            }
            0x02.toByte() -> { // FILE
                out.add(content)
            }
            0x03.toByte() -> { // END
                out.add("FILE_TRANSFER_COMPLETE")
            }
        }
    }
}
