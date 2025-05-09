package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CamposConfig : AppCompatActivity() {

    var optConfig: Boolean = ConfigPerfil.optionCong
    private lateinit var userDao: UserDao

    private lateinit var titularDates: TextView
    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var nombreUser: String
    private lateinit var telefonoUser: String
    private lateinit var nombreCEUser: String
    private lateinit var telefonoCEUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_campos_config)

        titularDates = findViewById(R.id.tituloDatos)
        nameEditText = findViewById(R.id.nameText)
        phoneEditText = findViewById(R.id.phoneText)
        saveButton = findViewById(R.id.saveButton)

        val db = AppDatabase.getDatabase(applicationContext)
        userDao = db.userDao()

        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Restaurar el estado del EditText al obtener el foco
                nameEditText.setBackgroundResource(R.drawable.registro_nombre)
                nameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
                nameEditText.setHint(R.string.nombre_Apellido)
            }
        }
        phoneEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Restaurar el estado del EditText al obtener el foco
                phoneEditText.setBackgroundResource(R.drawable.registro_nombre)
                phoneEditText.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
                phoneEditText.setHint(R.string.numero_celular)
            }
        }


        if (optConfig){
            titularDates.setText(R.string.datos_Personales)
            CoroutineScope(Dispatchers.IO).launch {
                val savedUser = userDao.getUser()
                if (savedUser != null) {
                    withContext(Dispatchers.Main) {
                        nameEditText.text = Editable.Factory.getInstance().newEditable(savedUser.name)
                        phoneEditText.text = Editable.Factory.getInstance().newEditable(savedUser.phone)
                        nombreCEUser = savedUser.nameCE
                        telefonoCEUser = savedUser.phoneCE
                    }
                }
            }

            saveButton.setOnClickListener{
                if(nameEditText.text.isEmpty() || phoneEditText.text.isEmpty()){
                    if (nameEditText.text.isEmpty()){
                        nameEditText.setBackgroundResource(R.drawable.registro_nombre_empty)
                        nameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.rojo_falla))
                        nameEditText.setHint("Por favor completa la casilla")
                    }
                    if (phoneEditText.text.isEmpty()){
                        phoneEditText.setBackgroundResource(R.drawable.registro_nombre_empty)
                        phoneEditText.setHintTextColor(ContextCompat.getColor(this, R.color.rojo_falla))
                        phoneEditText.setHint("Por favor completa la casilla")
                    }
                    Toast.makeText(this, "Por favor complete los campos solicitados", Toast.LENGTH_SHORT).show()
                }else{
                    if(esTelefonoValido(phoneEditText.text.toString()) && esNombreValido(nameEditText.text.toString())){
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = User(
                                id = 1,
                                name = nameEditText.text.toString(),
                                phone = phoneEditText.text.toString(),
                                nameCE = nombreCEUser,
                                phoneCE = telefonoCEUser
                            )
                            userDao.insertUser(user)}
                        Toast.makeText(this, "Datos Guardados", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ConfigPerfil::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        if (!esTelefonoValido(phoneEditText.text.toString())){
                            Toast.makeText(this, "Por favor introduzca un número de telefono valido", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this, "Por favor introduzca un nombre valido, no utilice simbolos especiales", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }else{
            titularDates.setText(R.string.datos_CEmergencia)
            CoroutineScope(Dispatchers.IO).launch {
                val savedUser = userDao.getUser()
                if (savedUser != null) {
                    withContext(Dispatchers.Main) {
                        nombreUser = savedUser.name
                        telefonoUser = savedUser.phone
                        nameEditText.text = Editable.Factory.getInstance().newEditable(savedUser.nameCE)
                        phoneEditText.text = Editable.Factory.getInstance().newEditable(savedUser.phoneCE)
                    }
                }
            }

            saveButton.setOnClickListener{
                if(nameEditText.text.isEmpty() || phoneEditText.text.isEmpty()){
                    if (nameEditText.text.isEmpty()){
                        nameEditText.setBackgroundResource(R.drawable.registro_nombre_empty)
                        nameEditText.setHintTextColor(ContextCompat.getColor(this, R.color.rojo_falla))
                        nameEditText.setHint("Por favor completa la casilla")
                    }
                    if (phoneEditText.text.isEmpty()){
                        phoneEditText.setBackgroundResource(R.drawable.registro_nombre_empty)
                        phoneEditText.setHintTextColor(ContextCompat.getColor(this, R.color.rojo_falla))
                        phoneEditText.setHint("Por favor completa la casilla")
                    }
                    Toast.makeText(this, "Por favor complete los campos solicitados", Toast.LENGTH_SHORT).show()
                }else{
                    if(esTelefonoValido(phoneEditText.text.toString()) && esNombreValido(nameEditText.text.toString())){
                        CoroutineScope(Dispatchers.IO).launch {
                            val user = User(
                                id = 1,
                                name = nombreUser,
                                phone =telefonoUser,
                                nameCE = nameEditText.text.toString(),
                                phoneCE = phoneEditText.text.toString()
                            )
                            userDao.insertUser(user)}
                        Toast.makeText(this, "Datos Guardados", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, ConfigPerfil::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        if (!esTelefonoValido(phoneEditText.text.toString())){
                            Toast.makeText(this, "Por favor introduzca un número de telefono valido", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(this, "Por favor introduzca un nombre valido, no utilice simbolos especiales", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }

    }

    fun esTelefonoValido(numero: String): Boolean {
        val regex = Regex("^3[0-9]{9}\$") // Verifica que el número que ingrese el usuario empiece por 3 y tenga 10 digitos
        return regex.matches(numero)
    }

    fun esNombreValido(nombre: String): Boolean {
        // Solo letras y espacios, mínimo 1 carácter
        val regex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+\$")
        return regex.matches(nombre)
    }

}