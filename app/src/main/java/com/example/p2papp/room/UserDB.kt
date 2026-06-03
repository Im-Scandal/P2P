package com.example.p2papp.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User (
    @PrimaryKey val id: Int = 1, // Siempre será 1 porque es un solo usuario
    val name: String,
    val phone: String,
    val nameCE: String,
    val phoneCE: String
)