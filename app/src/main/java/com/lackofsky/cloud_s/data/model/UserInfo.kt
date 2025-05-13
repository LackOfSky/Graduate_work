package com.lackofsky.cloud_s.data.model

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "usersInfo",
    foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = ["uniqueId"],
    childColumns = ["userId"],
    onDelete = ForeignKey.CASCADE)]
)
data class UserInfo (
    @PrimaryKey
    @ColumnInfo(name = "userId")
    val userId: String,
    @ColumnInfo(name = "userAbout")
    var about: String = "",
    @ColumnInfo(name = "userInfo")
    var info: String = "",
    @ColumnInfo(name = "userIcon")
    var iconImgURI: String?,
    @ColumnInfo(name = "userBanner")
    var bannerImgURI: String?
)
