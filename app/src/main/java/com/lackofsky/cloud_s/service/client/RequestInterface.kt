package com.lackofsky.cloud_s.service.client

import com.lackofsky.cloud_s.data.model.Message
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.service.model.MessageKey
import com.lackofsky.cloud_s.service.model.MessageType

interface StrangerRequestInterface {
    fun sendFriendRequest(sendTo: User):Boolean    //Request.ADD
    fun approveFriendRequest(sendTo: User):Boolean //Request.APPROVE
    fun cancelFriendRequest(sendTo: User):Boolean   //Request.CANCEL
    fun rejectFriendRequest(sendTo: User):Boolean   //Request.REJECT
    //fun deleteFriendRequest(sendTo: User):Boolean   //Request.DELETE

}
interface FriendRequestInterface {
    fun deleteFriendRequest(sendTo: NettyClient):Boolean

    //fun deleteFriendRequest(sendTo: User):Boolean   //Request.DELETE

}

interface ChangesNotifierRequestInterface {
    fun userChangesNotifierRequest(sendTo: List<NettyClient>, user: User): Boolean
    fun userInfoChangesNotifierRequest(sendTo: List<NettyClient>, userInfo: UserInfo): Boolean
    //fun defaultNotifierRequest(sendTo: List<NettyClient>, content: String,messageType: MessageType):Boolean
}

interface MessageRequestInterface {

    fun sendMessageRequest(sendTo: NettyClient, content: Message):Boolean
    //fun sendMessageRequest(sendTo: List<User>, content: Message):Boolean
    fun deleteMessageOne2OneRequest(sendTo: NettyClient,  messageKey: MessageKey):Boolean //todo знайти більш вдале рішення
    //fun updateMessageOne2OneRequest(sendTo: NettyClient, message: Message):Boolean
}