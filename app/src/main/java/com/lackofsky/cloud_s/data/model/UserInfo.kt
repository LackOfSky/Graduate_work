package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "usersInfo",
    foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = ["userId"],
    childColumns = ["userId"],
    onDelete = ForeignKey.CASCADE)]
)
data class UserInfo (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "userId")
    val id: Int = 0,
    @ColumnInfo(name = "userAbout")
    var about: String = "",
    @ColumnInfo(name = "userInfo")
    var info: String = "",
    @ColumnInfo(name = "userIcon")
    var iconImg: ByteArray = ByteArray(0),
    @ColumnInfo(name = "userBanner")
    var bannerImg: ByteArray = ByteArray(0)
)
