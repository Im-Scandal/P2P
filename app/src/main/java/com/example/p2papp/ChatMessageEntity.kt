package com.example.p2papp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameUser: String,
    val text: String,
    val timeSend: String,
    val timeReceived: String,
    val isSentByMe: Boolean
)
