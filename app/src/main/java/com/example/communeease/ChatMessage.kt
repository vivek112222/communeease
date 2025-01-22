package com.example.communeease

data class ChatMessage(
    val senderNickname: String = "",
    val messageText: String = "",
    val timestamp: Long = 0L
)
