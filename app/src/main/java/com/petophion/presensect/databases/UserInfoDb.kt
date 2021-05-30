package com.petophion.presensect.databases

import com.google.firebase.Timestamp

data class UserInfoDb(
    val userUID: String? = "",
    val username: String? = "",
    val first: String? = "",
    val last: String? = "",
    val bio: String? = "",
    val profilePicture: String? = ""
)
