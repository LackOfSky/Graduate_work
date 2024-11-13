package com.lackofsky.cloud_s.service.server

import com.lackofsky.cloud_s.data.model.User

interface ResponseInterface {
    fun addedFriendResponse(user: User):Boolean       //Response.ADDED
    fun approvedFriendResponse(user: User):Boolean    //Response.APPROVED
    fun canceledFriendResponse(user: User):Boolean    //Response.CANCELED
    fun rejectedFriendResponse(user: User):Boolean    //Response.REJECTED
    fun deletedFriendResponse(user: User):Boolean     //Response.DELETED
}