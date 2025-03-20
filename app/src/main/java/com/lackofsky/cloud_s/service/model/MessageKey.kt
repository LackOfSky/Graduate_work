package com.lackofsky.cloud_s.service.model

import java.util.Date

data class MessageKey(val chatName: String,
                      val contentHashCode: Int,
                      val date: Date)
