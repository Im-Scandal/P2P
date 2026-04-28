package com.example.p2papp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.p2papp.NetworkManager.Companion.lastBestLocation

class MainMenu : AppCompatActivity() {
    private lateinit var ayudaButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton
    private lateinit var radarButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        ayudaButton = findViewById(R.id.helpButton)
        perfilButton = findViewById(R.id.perfilButton)
        bibliotecaButton = findViewById(R.id.bibliotecaButton)
        radarButton = findViewById(R.id.radarButton)

        setOnListener()
        startGpsTracking()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val overlayView = LayoutInflater.from(this@MainMenu).inflate(R.layout.confirmar_salida, null)

                val rootView = findViewById<ViewGroup>(android.R.id.content)
                rootView.addView(overlayView)

                val closeApp = overlayView.findViewById<Button>(R.id.closeAppButton)
                closeApp.setOnClickListener{
                    finishAffinity()
                }

                val closeButton = overlayView.findViewById<Button>(R.id.closeWarningButton)
                closeButton.setOnClickListener {
                    rootView.removeView(overlayView)
                }
            }
        })
    }

    private fun setOnListener() {
        ayudaButton.setOnClickListener{
            val overlayView = LayoutInflater.from(this@MainMenu).inflate(R.layout.confirmacion_chat, null)
            val texto = overlayView.findViewById<TextView>(R.id.texto)
            texto.text = getString(R.string.zona_chat)
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
        perfilButton.setOnClickListener{
            val intent = Intent(this, ConfigPerfil::class.java)
            startActivity(intent)
        }
        bibliotecaButton.setOnClickListener{
            val intent = Intent(this, Biblioteca::class.java)
            startActivity(intent)
        }
        radarButton.setOnClickListener {
            val intent = Intent(this, RadarActivity::class.java)
            startActivity(intent)
        }
    }

    fun startGpsTracking() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000L,
                1f,
                NetworkManager.locationListener
            )

            lastBestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.i("Obteniendo GPS", "GPS ACTUALIZADO EN MAIN MENU ${lastBestLocation?.latitude}, ${lastBestLocation?.longitude}")
        }
    }
}