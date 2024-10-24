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
    var fullName: String,
    @ColumnInfo(name = "userLogin")
    var login: String,
    @ColumnInfo(name = "uniqueID") //replace to uniqueID
    var uniqueID: String = "",
    @Ignore
    var ipAddr: String = "",
    @Ignore
    var port: Int = 0){
    constructor(id: Int, fullName: String, login: String, uniqueID: String) : this(
        id, fullName, login, uniqueID, "", 0
    )
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        return uniqueID == other.uniqueID
    }

    override fun hashCode(): Int {
        return uniqueID.hashCode()
    }
}


    /*** поче*/