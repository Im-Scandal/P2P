package com.example.p2papp.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.p2papp.room.ChatMessageEntity

@Dao
interface MessagesDao {
    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    @Query("DELETE FROM chat_messages")
    suspend fun clearMessages()
}