package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainMenu : AppCompatActivity() {
    private lateinit var ayudaButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton
    private lateinit var radarButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        ayudaButton = findViewById(R.id.helpButton)
        perfilButton = findViewById(R.id.perfilButton)
        bibliotecaButton = findViewById(R.id.bibliotecaButton)
        radarButton = findViewById(R.id.radarButton)

        setOnListener()
    }

    private fun setOnListener() {
        ayudaButton.setOnClickListener{
            val intentChat = Intent(this, MainChat::class.java)
            startActivity(intentChat)
        }
        perfilButton.setOnClickListener{
            Toast.makeText(this, "Funciona Perfil", Toast.LENGTH_SHORT).show()
        }
        bibliotecaButton.setOnClickListener{
            Toast.makeText(this, "Funciona Biblio", Toast.LENGTH_SHORT).show()
        }
        radarButton.setOnClickListener{
            Toast.makeText(this, "Funciona Radar", Toast.LENGTH_SHORT).show()
        }
    }
}