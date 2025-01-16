package com.lackofsky.cloud_s.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    indices = [Index(value = ["uniqueId"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "userId")
    val id: Int = 0,
    @ColumnInfo(name = "userName")
    val fullName: String,
    @ColumnInfo(name = "userLogin")
    val login: String,
    @ColumnInfo(name = "uniqueId") //
    val uniqueID: String,
    @Ignore
    val ipAddr: String = "",
    @Ignore
    val port: Int = 15015){
    constructor(id: Int, fullName: String, login: String, uniqueID: String) : this(
        id, fullName, login, uniqueID, "", 0
    )
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other !is User) return false
//        return uniqueID == other.uniqueID
//    }
//    override fun hashCode(): Int {
//        return uniqueID.hashCode()
//    }
}


    /*** поче*/