package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SolicitedName: AppCompatActivity()
{
    private lateinit var nextButton: Button
    private lateinit var nameText: EditText

    companion object{
        var nameUser: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.solicitud_nombre)

        nextButton = findViewById(R.id.nextButton)
        nameText = findViewById(R.id.nameWText)

        nextButton.setOnClickListener{
            if (nameText.text.isNotEmpty()){
                nameUser = nameText.text.toString()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else {
                Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_LONG).show()
            }
        }
    }
}