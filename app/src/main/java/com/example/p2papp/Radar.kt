package com.example.p2papp

import OverlayManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton

class RadarActivity : AppCompatActivity() {

    private lateinit var  homeButton: ImageButton
    private lateinit var helpButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton

    private lateinit var overlayManager: OverlayManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radar) // XML del radar



        val rootView = findViewById<View>(android.R.id.content) as ViewGroup
        overlayManager = OverlayManager(this, rootView)

        initialWork()
        setOnListener()
    }

    private fun initialWork() {
        homeButton = findViewById(R.id.homeButton)
        helpButton = findViewById(R.id.helpButton)
        perfilButton = findViewById(R.id.perfilButton)
        bibliotecaButton = findViewById(R.id.bibliotecaButton)

    }

    private fun setOnListener(){
        helpButton.setOnClickListener{
            val overlayView = LayoutInflater.from(this@RadarActivity).inflate(R.layout.confirmacion_chat, null)

            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(overlayView)

            val closeApp = overlayView.findViewById<Button>(R.id.siButton)
            closeApp.setOnClickListener{
                rootView.removeView(overlayView)
                val intent = Intent(this, animacionChat::class.java)
                startActivity(intent)
            }

            val closeButton = overlayView.findViewById<Button>(R.id.noButton)
            closeButton.setOnClickListener {
                rootView.removeView(overlayView)
            }
        }
        homeButton.setOnClickListener{
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
        bibliotecaButton.setOnClickListener {
            val intent = Intent(this, Biblioteca::class.java)
            startActivity(intent)
        }
        perfilButton.setOnClickListener {
            val intent = Intent(this, ConfigPerfil::class.java)
            startActivity(intent)
        }
    }

}