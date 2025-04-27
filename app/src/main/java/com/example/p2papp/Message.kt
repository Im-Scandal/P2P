package com.example.p2papp

// Nuevo modelo Message.kt
data class ChatMessage(
    val text: String,
    val timeSend: String,
    val timeReceived: String,
    val isSentByMe: Boolean
)

