package com.example.p2papp

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfigPerfil : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var userDao: UserDao

    lateinit var nameUser: TextView
    private lateinit var phoneUser: TextView
    private lateinit var nameCEUser: TextView
    private lateinit var phoneCEUser: TextView

    private lateinit var personalButton: Button
    private lateinit var contactButton: Button

    companion object {
        var optionCong: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_perfil)
        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)

        val db = AppDatabase.getDatabase(applicationContext)
        userDao = db.userDao()

        initial_work()
        dataBase_EditText()

        configureTextViewBackground(nameUser)
        configureTextViewBackground(phoneUser)
        configureTextViewBackground(nameCEUser)
        configureTextViewBackground(phoneCEUser)

        setOnListener()

        // Configurar el OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ConfigPerfil, MainMenu::class.java)
                startActivity(intent)
            }
        })
    }

    private fun setOnListener() {
        personalButton.setOnClickListener{
            optionCong = true
            val intent = Intent(this, CamposConfig::class.java)
            startActivity(intent)
        }
        contactButton.setOnClickListener{
            optionCong = false
            val intent = Intent(this, CamposConfig::class.java)
            startActivity(intent)
        }
    }

    private fun initial_work() {
        nameUser = findViewById(R.id.nameUserText)
        phoneUser = findViewById(R.id.phoneUserText)
        nameCEUser = findViewById(R.id.nameCEUserText)
        contactButton= findViewById(R.id.contactButton)
        phoneCEUser = findViewById(R.id.phoneCEUserText)
        personalButton = findViewById(R.id.personalButton)
    }

    private fun dataBase_EditText() {
        CoroutineScope(Dispatchers.IO).launch {
            val savedUser = userDao.getUser()
            if (savedUser != null) {
                nameUser.text = savedUser.name
                phoneUser.text = savedUser.phone
                nameCEUser.text = savedUser.nameCE
                phoneCEUser.text = savedUser.phoneCE
            }
        }
    }

    private fun configureTextViewBackground(textView: TextView) {
        if (textView.text.isEmpty()) {
            textView.setBackgroundResource(R.drawable.registro_nombre)
        } else {
            textView.setBackgroundResource(R.drawable.campo_perfil_lleno)
        }
    }

}