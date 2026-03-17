package com.example.p2papp

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class animacionChat : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var wifiManager: WifiManager
    private val imageList = listOf(
        R.drawable.property_1_default,
        R.drawable.property_1_variant2,
        R.drawable.property_1_variant3,
        R.drawable.property_1_variant4,
        R.drawable.property_1_variant5,
        R.drawable.property_1_variant6,
        R.drawable.property_1_variant7,
        R.drawable.property_1_variant8,
        R.drawable.property_1_variant9
    )

    private val settingsPanelLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // After the user closes the panel, check if Wi-Fi is enabled
        // and proceed to the next activity.
        if (wifiManager.isWifiEnabled) {
            goToMainChat()
        } else {
            // Si NO lo activó, le mostramos un diálogo explicativo
            mostrarDialogoWifiRequerido()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animacion_chat)

        imageView = findViewById(R.id.animacionImage)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        mostrarImagenesSecuencialmente()
    }

    private fun mostrarImagenesSecuencialmente() {
        var index = 0

        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                if (index < imageList.size) {
                    imageView.setImageResource(imageList[index])
                    index++
                    handler.postDelayed(this, 500)
                } else {
                    // When the animation finishes, check and activate Wi-Fi
                    checkAndActivateWifi()
                }
            }
        }

        handler.post(runnable)
    }

    private fun checkAndActivateWifi() {
        if (wifiManager.isWifiEnabled) {
            // If Wi-Fi is already on, just go to the chat
            goToMainChat()
        } else {
            // If Wi-Fi is off, prompt the user to turn it on
            Toast.makeText(this, "Es necesario ativar el Wi-Fi", Toast.LENGTH_SHORT).show()
            activarWifi()
        }
    }


        private fun activarWifi() {
            // For Android 10 (Q) and above, use the Settings Panel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val intent = Intent(Settings.Panel.ACTION_WIFI)
                settingsPanelLauncher.launch(intent)
            }
        }

    private fun mostrarDialogoWifiRequerido() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wi-Fi Requerido")
        builder.setMessage("Para usar las funciones de chat, es necesario que actives la conexión Wi-Fi. ¿Quieres intentarlo de nuevo?")

        // Botón para reintentar: vuelve a mostrar el panel de Wi-Fi
        builder.setPositiveButton("Reintentar") { dialog, _ ->
            dialog.dismiss()
            activarWifi()
        }

        // Botón para salir: cierra la aplicación
        builder.setNegativeButton("Salir") { dialog, _ ->
            dialog.dismiss()
            finish() // Cierra la actividad actual y, por ende, la app
        }

        // Evita que el diálogo se cierre si el usuario toca fuera de él
        builder.setCancelable(false)

        builder.show()
    }

    private fun goToMainChat() {
        val intent = Intent(this@animacionChat, MainChat::class.java)
        startActivity(intent)
        finish() // Also, finish this activity so the user can't go back to the animation
    }
}
