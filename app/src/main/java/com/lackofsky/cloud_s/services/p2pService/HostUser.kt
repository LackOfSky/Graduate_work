package com.lackofsky.cloud_s.services.p2pService

import com.adroitandroid.near.model.Host
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo

data class HostUser(val host: Host,
                    val user: User,
                    var userInfo: UserInfo? = null) {
}