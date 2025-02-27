package com.example.communeease

data class Notification(
    val senderId: String,
    val senderUsername: String = "",
    val profileImageIndex: String = "0",
    val status: String = ""
)
