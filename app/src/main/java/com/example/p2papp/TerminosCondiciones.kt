package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TerminosCondiciones : AppCompatActivity() {

    private lateinit var acceptButton: Button
    private lateinit var acceptCheck: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terminos_condiciones)

        acceptButton = findViewById(R.id.button_accept)
        acceptCheck = findViewById(R.id.checkbox_accept_terms)

        acceptButton.setOnClickListener{
            if (acceptCheck.isChecked){
                val intent = Intent(this, MainMenu::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Debes aceptar los términos y condiciones.", Toast.LENGTH_LONG).show()
            }
        }
    }
}