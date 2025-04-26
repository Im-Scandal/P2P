package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainMenu : AppCompatActivity() {
    private lateinit var ayudaButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        ayudaButton = findViewById(R.id.helpButton)
        perfilButton = findViewById(R.id.perfilButton)
        bibliotecaButton = findViewById(R.id.bibliotecaButton)

        setOnListener()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Mostrar un diálogo de confirmación antes de regresar
                val builder = AlertDialog.Builder(this@MainMenu)
                builder.setTitle("Confirmación")
                builder.setMessage("¿Estás seguro de que quieres salir de la aplicación?")
                builder.setPositiveButton("Sí") { _, _ ->
                    // Si el usuario confirma, permitir la acción de regresar
                    finishAffinity()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss() // Cancelar y permanecer en la actividad
                }
                builder.show()
            }
        })
    }

    private fun setOnListener() {
        ayudaButton.setOnClickListener{
            val intent = Intent(this, MainChat::class.java)
            startActivity(intent)
        }
        perfilButton.setOnClickListener{
            val intent = Intent(this, ConfigPerfil::class.java)
            startActivity(intent)
        }
        bibliotecaButton.setOnClickListener{
            Toast.makeText(this, "Funciona Biblio", Toast.LENGTH_SHORT).show()
        }
    }
}