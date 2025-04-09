package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SolicitedName: AppCompatActivity()
{
    private lateinit var nextButton: Button
    private lateinit var nameText: EditText
    private lateinit var infoButton: Button

    companion object{
        var nameUser: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_inicio)

        nextButton = findViewById(R.id.nextButton)
        nameText = findViewById(R.id.nameWText)
        infoButton = findViewById(R.id.infoView)

        nameText.setOnClickListener{
            nameText.setBackgroundResource(R.drawable.registro_nombre)
            nameText.setHintTextColor(
                ContextCompat.getColor(this, R.color.hint))
            nameText.setHint(R.string.name_register)
        }
        nextButton.setOnClickListener{
            if (nameText.text.isNotEmpty()){
                nameUser = nameText.text.toString()
                val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPreferences.edit().putString("userName", nameUser).apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }else {
                Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_LONG).show()
                nameText.setBackgroundResource(R.drawable.registro_nombre_empty)
                nameText.setHintTextColor(
                    ContextCompat.getColor(this, R.color.rojo_falla))
                nameText.setHint("Por favor completa la casilla")
            }
        }
        infoButton.setOnClickListener{
            
        }

    }
}