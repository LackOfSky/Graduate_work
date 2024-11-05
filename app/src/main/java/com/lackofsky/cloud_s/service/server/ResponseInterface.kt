package com.lackofsky.cloud_s.service.server

import com.lackofsky.cloud_s.data.model.User

interface ResponseInterface {

    fun approveFriendResponse(user: User):Boolean    //Response.APPROVE
    fun cancelFriendResponse(user: User):Boolean   //Response.CANCELED
    fun rejectFriendResponse(user: User):Boolean   //Response.REJECTED
    fun deleteFriendResponse(user: User):Boolean   //Response.DELETED
}