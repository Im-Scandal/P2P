package com.example.p2papp

data class ChatMessage(
    val nameUser: String,
    val text: String,
    val timeSend: String,
    val timeReceived: String,
    val isSentByMe: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val distance: String? = null
)

