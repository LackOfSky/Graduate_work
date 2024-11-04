package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    val id: Int = 0,
    @ColumnInfo(name = "userName")
    val fullName: String,
    @ColumnInfo(name = "userLogin")
    val login: String,
    @ColumnInfo(name = "uniqueID") //replace to uniqueID
    val uniqueID: String = "",
    @Ignore
    val ipAddr: String = "",
    @Ignore
    val port: Int = 15015){
    constructor(id: Int, fullName: String, login: String, uniqueID: String) : this(
        id, fullName, login, uniqueID, "", 0
    )
}


    /*** поче*/