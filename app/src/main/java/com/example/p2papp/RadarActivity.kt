package com.example.p2papp

import OverlayManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.p2papp.Constants.TAG_WIFI
import kotlinx.coroutines.*

class RadarActivity : AppCompatActivity() {

    private lateinit var userName: String
    private var radarJob: Job? = null
    private lateinit var  homeButton: ImageButton
    private lateinit var helpButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton

    private lateinit var overlayManager: OverlayManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radar)


        val rootView = findViewById<View>(android.R.id.content) as ViewGroup
        overlayManager = OverlayManager(this, rootView)

        initialWork()
        setOnListener()
    }

//    override fun onResume() {
//        super.onResume()
//        startTransmittingLocation() // Comienza a transmitir cuando abres la pantalla
//    }
//
//    override fun onPause() {
//        super.onPause()
//        radarJob?.cancel() // Detiene la transmisión si minimizas o sales de la pantalla
//    }
//
//    private fun loadUserNameFromDatabase(onLoaded: () -> Unit) {
//        val db = AppDatabase.getDatabase(this)
//        val userDao = db.userDao()
//        CoroutineScope(Dispatchers.IO).launch {
//            val savedUser = userDao.getUser()
//            if (savedUser != null) {
//                userName = savedUser.name.trim()
//            }
//
//            val savedMessages = db.chatMessageDao().getAllMessages()
//        }
//    }
//
//    private fun startTransmittingLocation() {
//        radarJob = lifecycleScope.launch(Dispatchers.IO) {
//            while (isActive) { // Mientras la corrutina no sea cancelada
//                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//                if (ActivityCompat.checkSelfPermission(this as Context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    val lastLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                    val myLat = lastLocation?.latitude
//                    val myLon = lastLocation?.longitude
//
//                    Log.e(TAG_WIFI, "latitud: $myLat, longitud: $myLon")
//
//                        if (myLat != null && myLon != null) {
//                            val radarFrame = WifiFrame().apply {
//                                nameUser = userName
//                                type = "RADAR" // Marcamos que es un ping de ubicación
//                                latitude = myLat
//                                longitude = myLon
//                            }
//
//                            // Llamas a tu función de red para enviar el paquete
//                            //sendWifiFrameOverNetwork(radarFrame)
//                        }
//
//                    delay(5000) // intervalo de cada 5 segundos
//
//            }else{
//                    Log.e(TAG_WIFI, "no se pudo obtener la ubicación")
//            }
//        }
//    }
//}

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