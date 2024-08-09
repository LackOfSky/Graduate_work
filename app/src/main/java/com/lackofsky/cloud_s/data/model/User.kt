package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    @SerializedName("userId")
    val id: Int = 0,
    @ColumnInfo(name = "userName")
    var fullName: String,
    @ColumnInfo(name = "userLogin")
    var login: String,
    @ColumnInfo(name = "userAbout")
    var about: String = "",
    @ColumnInfo(name = "userInfo")
    var info: String = "",
    @ColumnInfo(name = "userIcon")
    var iconImg: ByteArray = ByteArray(0),
    @ColumnInfo(name = "userBanner")
    var bannerImg: ByteArray = ByteArray(0)
)