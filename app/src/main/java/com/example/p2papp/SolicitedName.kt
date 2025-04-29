package com.example.p2papp

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SolicitedName: AppCompatActivity() {
    private lateinit var nextButton: Button
    private lateinit var nameText: EditText
    private lateinit var infoButton: ImageButton

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_inicio)

        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val checkboxState = sharedPreferences.getBoolean("CheckboxState", false)

        requestPermissions()

        if (checkboxState) {
            // Si el checkbox ya fue marcado, redirige a la siguiente Activity.
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
            finish()
        } else {
            nextButton = findViewById(R.id.nextButton)
            nameText = findViewById(R.id.nameWText)
            infoButton = findViewById(R.id.infoView)

            val db = AppDatabase.getDatabase(applicationContext)
            userDao = db.userDao()

            nameText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Restaurar el estado del EditText al obtener el foco
                    nameText.setBackgroundResource(R.drawable.registro_nombre)
                    nameText.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
                    nameText.setHint(R.string.name_register)
                }
            }

            nextButton.setOnClickListener {
                if (nameText.text.isNotEmpty()) {
                    val userName = nameText.text.toString()
                    sharedPreferences.edit().putString("userName", userName).apply()


                    // Llamar al insertUser desde una corrutina
                    CoroutineScope(Dispatchers.IO).launch {
                        val user = User(id = 1, name = userName, phone = "", nameCE = "", phoneCE = "")
                        userDao.insertUser(user)
                        // Verificamos que se haya guardado consultándolo
                        val savedUser = userDao.getUser()

                        if (savedUser != null) {
                            if (savedUser.name.isNotEmpty()) {
                                runOnUiThread {
                                    Toast.makeText(this@SolicitedName, "Usuario guardado: ${savedUser.name}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    val intent = Intent(this, TerminosCondiciones::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Mostrar estado de error si el campo está vacío
                    Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_LONG).show()
                    nameText.setBackgroundResource(R.drawable.registro_nombre_empty)
                    nameText.setHintTextColor(ContextCompat.getColor(this, R.color.rojo_falla))
                    nameText.setHint("Por favor completa la casilla")
                }
            }

            infoButton.setOnClickListener {
                val overlayView = LayoutInflater.from(this).inflate(R.layout.ingresa_info, null)

                val rootView = findViewById<ViewGroup>(android.R.id.content)
                rootView.addView(overlayView)

                val closeButton = overlayView.findViewById<ImageButton>(R.id.closeButton)
                closeButton.setOnClickListener {
                    rootView.removeView(overlayView)
            }}
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Agregar permisos según la versión del SDK
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Registro del callback para múltiples permisos
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (isGranted) {
                // El permiso ha sido concedido
                println("$permission concedido.")
            } else {
                // El permiso ha sido denegado
                println("$permission denegado.")
            }
        }
    }

}