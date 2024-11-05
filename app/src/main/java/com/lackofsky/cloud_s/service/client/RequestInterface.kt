package com.lackofsky.cloud_s.service.client

import com.lackofsky.cloud_s.data.model.User

interface RequestInterface {
    fun sendFriendRequest(user: User):Boolean    //Request.ADD
    fun cancelFriendRequest(user: User):Boolean   //Request.CANCEL
    fun rejectFriendRequest(user: User):Boolean   //Request.REJECT
    fun deleteFriendRequest(user: User):Boolean   //Request.DELETE

}