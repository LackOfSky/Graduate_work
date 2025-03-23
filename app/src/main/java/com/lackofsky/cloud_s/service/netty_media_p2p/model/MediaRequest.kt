package com.lackofsky.cloud_s.service.netty_media_p2p.model

data class MediaRequest(    val fileName: String?,
                            val messageId: String = "", //now for MEDIA_EXTERNAL
                            val mimeType: String,
                            val fileSize: Long,
                            val checksum: String?,       // Контрольна сума файлу (наприклад, SHA-256 для перевірки цілісності)
                            val chunkSize: Int?,          // Розмір одного пакету (опціонально, якщо передається частинами)
                            val transferMode: TransferMode,    // "streaming" або "multipart" (щоб знати, як серверу обробляти файл)
                            val senderId: String,       // Ідентифікатор відправника (якщо потрібно)
                            val transferMediaIntend: TransferMediaIntend
)

enum class TransferMode { STREAMING, MULTIPART }
enum class TransferMediaIntend{MEDIA_USER_LOGO, MEDIA_USER_BANNER, MEDIA_EXTERNAL}//TODO MEDIA_INTERNAL (В КЕШ ЗАСТОСУНКУ) todo SEND_POST