package com.example.communeease

data class FriendChat(
    val messageId: String = "",
    val senderNickname: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)
