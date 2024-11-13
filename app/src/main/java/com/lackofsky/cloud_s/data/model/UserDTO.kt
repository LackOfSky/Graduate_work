package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Ignore

data class UserDTO(
    val id: Int = 0,
    val fullName: String,
    val login: String,
    val uniqueID: String = "")