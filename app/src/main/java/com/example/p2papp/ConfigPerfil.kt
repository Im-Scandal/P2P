package com.example.p2papp

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ConfigPerfil : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    var userName: String = SolicitedName.nameUser
    private lateinit var nameUser: TextView
    private lateinit var phoneUser: TextView
    private lateinit var nameCEUser: TextView
    private lateinit var phoneCEUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_perfil)
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)

        initial_work()

        nameUser.setText(userName)

        comprobacion_campos()
    }

    private fun initial_work() {
        nameUser = findViewById(R.id.nameUserText)
        phoneUser = findViewById(R.id.phoneUserText)
        nameCEUser = findViewById(R.id.nameCEUserText)
        phoneCEUser = findViewById(R.id.phoneCEUserText)
    }

    private fun comprobacion_campos() {
        if (nameUser.text.isEmpty()){
            nameUser.setBackgroundResource(R.drawable.registro_nombre)
        }else{
            nameUser.setBackgroundResource(R.drawable.campo_perfil_lleno)
        }
        if (phoneUser.text.isEmpty()){
            phoneUser.setBackgroundResource(R.drawable.registro_nombre)
        }else{
            phoneUser.setBackgroundResource(R.drawable.campo_perfil_lleno)
        }
        if (nameCEUser.text.isEmpty()){
            nameCEUser.setBackgroundResource(R.drawable.registro_nombre)
        }else{
            nameCEUser.setBackgroundResource(R.drawable.campo_perfil_lleno)
        }
        if (phoneCEUser.text.isEmpty()){
            phoneCEUser.setBackgroundResource(R.drawable.registro_nombre)
        }else{
            phoneCEUser.setBackgroundResource(R.drawable.campo_perfil_lleno)
        }
    }
}