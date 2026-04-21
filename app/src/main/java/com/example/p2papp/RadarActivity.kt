package com.example.p2papp

import OverlayManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.p2papp.Constants.TAG_WIFI
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

class RadarActivity : AppCompatActivity() {

    private var userName: String = "Usuario" // Valor por defecto
    private var radarJob: Job? = null

    private lateinit var homeButton: ImageButton
    private lateinit var helpButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton
    private lateinit var radarContainer: RelativeLayout // Contenedor del radar

    private lateinit var overlayManager: OverlayManager
    private var info: WifiFrame = WifiFrame()
    private lateinit var wifiManager: WifiManager
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel


    // Mapa para evitar duplicar iconos del mismo dispositivo. Clave: Nombre del usuario, Valor: ImageView
    private val activeDevicesOnRadar = mutableMapOf<String, ImageView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_radar)

        val rootView = findViewById<View>(android.R.id.content) as ViewGroup
        overlayManager = OverlayManager(this, rootView)

        initialWork()
        setOnListener()

        // Cargamos el usuario ANTES de empezar a transmitir
        loadUserNameFromDatabase {
            Log.d(TAG_WIFI, "Usuario cargado: $userName")
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RadarEvent.radarPings.collect { wifiFrame ->
                    handleIncomingRadarPing(wifiFrame)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startTransmittingLocation()
    }

    override fun onPause() {
        super.onPause()
        radarJob?.cancel()
    }

    private fun loadUserNameFromDatabase(onLoaded: () -> Unit) {
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            val savedUser = userDao.getUser()
            if (savedUser != null) {
                userName = savedUser.name.trim()
            }
            // Ejecutamos el callback en el hilo principal
            withContext(Dispatchers.Main) {
                onLoaded()
            }
        }
    }

    private fun startTransmittingLocation() {
        radarJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (ActivityCompat.checkSelfPermission(this@RadarActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val lastLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    val myLat = lastLocation?.latitude
                    val myLon = lastLocation?.longitude

                    Log.d(TAG_WIFI, "Mi latitud: $myLat, longitud: $myLon")

                    if (myLat != null && myLon != null) {
                        val radarFrame = WifiFrame().apply {
                            nameUser = userName
                            type = "RADAR"
                            latitude = myLat
                            longitude = myLon
                        }

                        sendWifiFrameOverNetwork(radarFrame)
                    }
                } else {
                    Log.e(TAG_WIFI, "No se pudo obtener la ubicación (Permisos faltantes)")
                }

                // Intervalo de 60 segundos
                delay(60000)
            }
        }
    }

    // --- LÓGICA DE DIBUJO DEL RADAR ---

    fun handleIncomingRadarPing(wifiFrame: WifiFrame) {
        val targetLat = wifiFrame.latitude
        val targetLon = wifiFrame.longitude
        val deviceName = wifiFrame.nameUser

        if (targetLat == null || targetLon == null || deviceName == userName) return

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val lastLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val myLat = lastLocation?.latitude
            val myLon = lastLocation?.longitude

            if (myLat != null && myLon != null) {
                // 1. Calcular distancia en metros (X, Y)
                val (xMeters, yMeters) = getRelativeCartesian(myLat, myLon, targetLat, targetLon)

                // 2. Dibujar en la interfaz
                runOnUiThread {
                    addOrUpdateDeviceOnRadar(xMeters, yMeters, deviceName)
                }
            }
        }
    }

    private fun addOrUpdateDeviceOnRadar(xMeters: Double, yMeters: Double, deviceName: String) {
        // Escala: 1 metro = 12 dp
        val scaleDpPerMeter = 12.0
        val xDp = xMeters * scaleDpPerMeter
        val yDp = yMeters * scaleDpPerMeter

        val xPx = dpToPx(xDp.toFloat())
        val yPx = dpToPx(-yDp.toFloat()) // Invertimos Y para que el Norte sea hacia arriba

        // Revisamos si el dispositivo ya está en el radar
        val existingIcon = activeDevicesOnRadar[deviceName]

        if (existingIcon != null) {
            // Si ya existe, solo actualizamos su posición con una pequeña animación
            existingIcon.animate()
                .translationX(xPx)
                .translationY(yPx)
                .setDuration(500)
                .start()
        } else {
            // Si es nuevo, creamos el ImageView
            val deviceIcon = ImageView(this).apply {
                setImageResource(R.drawable.dispositivoradar) // Tu icono
                contentDescription = deviceName

                setOnClickListener {
                    Toast.makeText(this@RadarActivity, "Usuario: $deviceName", Toast.LENGTH_SHORT).show()
                }
            }

            val iconSizePx = dpToPx(30f).toInt()
            val params = RelativeLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }

            deviceIcon.layoutParams = params
            deviceIcon.translationX = xPx
            deviceIcon.translationY = yPx

            radarContainer.addView(deviceIcon)
            activeDevicesOnRadar[deviceName] = deviceIcon // Lo guardamos en el mapa
        }
    }

    // --- FUNCIONES MATEMÁTICAS Y DE UTILIDAD ---

    private fun getRelativeCartesian(myLat: Double, myLon: Double, targetLat: Double, targetLon: Double): Pair<Double, Double> {
        val earthRadius = 6371000.0
        val dLat = Math.toRadians(targetLat - myLat)
        val dLon = Math.toRadians(targetLon - myLon)
        val myLatRad = Math.toRadians(myLat)

        val x = earthRadius * dLon * cos(myLatRad)
        val y = earthRadius * dLat

        return Pair(x, y)
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )
    }

    // --- INICIALIZACIÓN DE UI ---

    private fun initialWork() {
        homeButton = findViewById(R.id.homeButton)
        helpButton = findViewById(R.id.helpButton)
        perfilButton = findViewById(R.id.perfilButton)
        bibliotecaButton = findViewById(R.id.bibliotecaButton)
        radarContainer = findViewById(R.id.radarContainer)

        wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
    }

    private fun setOnListener() {
        helpButton.setOnClickListener {
            val overlayView = LayoutInflater.from(this@RadarActivity).inflate(R.layout.confirmacion_chat, null)
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(overlayView)

            val closeApp = overlayView.findViewById<Button>(R.id.siButton)
            closeApp.setOnClickListener {
                rootView.removeView(overlayView)
                val intent = Intent(this, animacionChat::class.java) // Verifica que esta clase exista
                startActivity(intent)
            }

            val closeButton = overlayView.findViewById<Button>(R.id.noButton)
            closeButton.setOnClickListener {
                rootView.removeView(overlayView)
            }
        }
        homeButton.setOnClickListener {
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

    // Función temporal para que el código compile. Reemplázala con tu lógica real de envío WiFi Direct.
    private fun sendWifiFrameOverNetwork(frame: WifiFrame) {
//        Log.d(TAG_WIFI, "Simulando envío de paquete RADAR...")
        info = WifiFrameUtils.buildMyWiFiFrame(this, userName)

        val record = WifiFrameUtils.wifiFrameToHashMap(info)

        val serviceInfo =
            WifiP2pDnsSdServiceInfo.newInstance("_networkChat", "_chatApp._tcp", record)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
                    )
        ) {

            Toast.makeText(this, "Faltan pemisos 1", Toast.LENGTH_SHORT).show()
            return
        }
        manager.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.d("TAG_RADAR", "Envio de paquete exitoso")
            }

            override fun onFailure(arg0: Int) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.e("TAG_RADAR", "Fallo en envio de paquete")
                Toast.makeText(this@RadarActivity, "Fallo en envio de paquete", Toast.LENGTH_SHORT).show()
            }
        })
    }
}