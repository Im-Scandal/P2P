package com.example.p2papp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class animacionChat : AppCompatActivity() {

    private lateinit var imageView: ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_animacion_chat)

        imageView = findViewById(R.id.animacionImage)

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
                    handler.postDelayed(this, 500) // 0.5 segundos
                } else {
                    // Cambiar de actividad cuando termina la secuencia
                    val intent = Intent(this@animacionChat, MainChat::class.java)
                    startActivity(intent)
                }
            }
        }

        handler.post(runnable)
    }
}
