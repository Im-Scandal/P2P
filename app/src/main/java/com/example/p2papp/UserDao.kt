package com.example.p2papp
// UserDao.kt
import androidx.room.*

@Dao
interface UserDao {

    // INSERTAR ESE NOMBRE
    // Inserta un usuario en la tabla.
    // Si ya hay un usuario con el mismo id, lo reemplaza automáticamente.
    // Esto es útil porque solo vamos a manejar un único usuario localmente.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // CONSULTARLO DESPUÉS
    // Recupera el usuario que tenga el ID 1.
    // Como solo hay un usuario (y siempre usamos el id 1), esto nos da su nombre.
    // Devuelve null si no hay ningún usuario todavía.
    @Query("SELECT * FROM user WHERE id = 1 LIMIT 1")
    suspend fun getUser(): User?

    // BORRARLO SI SE DESEA
    // Elimina todos los registros de la tabla `user`.
    // En tu caso, eso significa borrar al único usuario guardado.
    @Query("DELETE FROM user")
    suspend fun deleteAll()
}