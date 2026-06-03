package com.example.p2papp.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.p2papp.room.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertUser(user: User)


    @Query("SELECT * FROM user WHERE id = 1 LIMIT 1")
    suspend fun getUser(): User?


    @Query("DELETE FROM user")
    suspend fun deleteAll()
}