package com.lackofsky.cloud_s.data.model

data class User(
    var fullName: String,
    var login: String,
    var about: String = "",
    var info: String = ""
)