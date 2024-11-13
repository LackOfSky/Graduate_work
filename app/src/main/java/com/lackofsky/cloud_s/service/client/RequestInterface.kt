package com.lackofsky.cloud_s.service.client

import com.lackofsky.cloud_s.data.model.User

interface RequestInterface {
    fun sendFriendRequest(sendTo: User):Boolean    //Request.ADD
    fun approveFriendRequest(sendTo: User):Boolean //Request.APPROVE
    fun cancelFriendRequest(sendTo: User):Boolean   //Request.CANCEL
    fun rejectFriendRequest(sendTo: User):Boolean   //Request.REJECT
    fun deleteFriendRequest(sendTo: User):Boolean   //Request.DELETE

}