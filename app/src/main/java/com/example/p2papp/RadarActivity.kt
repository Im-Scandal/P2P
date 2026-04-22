package com.example.p2papp

import OverlayManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
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
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.p2papp.Constants.TAG_WIFI
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.p2papp.Constants.TAG
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class RadarActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var userName: String = "Usuario" // Valor por defecto
    private var radarJob: Job? = null

    private lateinit var homeButton: ImageButton
    private lateinit var helpButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton
    private lateinit var radarContainer: RelativeLayout // Contenedor del radar

    private lateinit var overlayManager: OverlayManager
    private var info: WifiFrame = WifiFrame()
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
            NetworkManager.setup(this)
            NetworkManager.addServiceRequest(this)
            NetworkManager.startDiscover(this)
        }

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                RadarEvent.radarPings.collect { wifiFrame ->
                    if (wifiFrame.type == "RADAR") {
                        handleIncomingRadarPing(wifiFrame)
                    }
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

                    val editor = sharedPreferences.edit()
                    editor.putString(Constants.MESSAGE, "RADAR")
                    if (myLat != null) editor.putString("LATITUDE", myLat.toString()) else editor.remove("LATITUDE")
                    if (myLon != null) editor.putString("LONGITUDE", myLon.toString()) else editor.remove("LONGITUDE")
                    editor.apply()

                    Log.d(TAG_WIFI, "Mi latitud: $myLat, longitud: $myLon")

                    if (myLat != null && myLon != null) {
                        sendWifiFrameOverNetwork()
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

//    private fun addOrUpdateDeviceOnRadar(xMeters: Double, yMeters: Double, deviceName: String) {
//        val testXDp = 50f  // 50 dp a la derecha del centro
//        val testYDp = -50f // 50 dp arriba del centro (Y es invertido)
//
//        val xPx = dpToPx(testXDp)
//        val yPx = dpToPx(testYDp)
//
//        // Revisamos si el dispositivo ya está en el radar
//        val existingIcon = activeDevicesOnRadar[deviceName]
//
//        if (existingIcon != null) {
//            // Si ya existe, animamos a la posición de prueba
//            existingIcon.animate()
//                .translationX(xPx)
//                .translationY(yPx)
//                .setDuration(500)
//                .start()
//        } else {
//            // Si es nuevo, creamos el ImageView
//            val deviceIcon = ImageView(this).apply {
//                setImageResource(R.drawable.dispositivoradar) // Tu icono
//                contentDescription = deviceName
//
//                setOnClickListener {
//                    Toast.makeText(this@RadarActivity, "Usuario: $deviceName", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            val iconSizePx = dpToPx(30f).toInt()
//
//            // Centramos el icono en el contenedor
//            val params = RelativeLayout.LayoutParams(iconSizePx, iconSizePx).apply {
//                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
//            }
//
//            deviceIcon.layoutParams = params
//
//            // Lo movemos a nuestra posición de prueba
//            deviceIcon.translationX = xPx
//            deviceIcon.translationY = yPx
//
//            radarContainer.addView(deviceIcon)
//            activeDevicesOnRadar[deviceName] = deviceIcon // Lo guardamos en el mapa
//
//            Log.d("PRUEBA_UI_RADAR", "Icono creado y añadido al contenedor para $deviceName en X:$xPx, Y:$yPx")
//        }
//    }

    private fun addOrUpdateDeviceOnRadar(xMeters: Double, yMeters: Double, deviceName: String) {
        // 1. Radio máximo del radar
        val maxRadarRadiusMeters = 100.0

        // 2. Obtenemos las dimensiones reales del contenedor en pantalla
        val containerWidth = radarContainer.width
        val containerHeight = radarContainer.height

        if (containerWidth == 0 || containerHeight == 0) {
            Log.w("GEOMETRIA_RADAR", "El contenedor aún no tiene dimensiones, saltando frame.")
            return
        }

        // El radio en píxeles es la mitad del ancho/alto del contenedor
        val containerRadiusPx = (Math.min(containerWidth, containerHeight) / 2f).toDouble()

        // 3. ¿Cuántos píxeles de pantalla equivalen a 1 metro real?
        val pixelsPerMeter = containerRadiusPx / maxRadarRadiusMeters

        // 4. Calculamos cuánto mover el icono desde el centro
        val targetX = (xMeters * pixelsPerMeter).toFloat()
        val targetY = (-yMeters * pixelsPerMeter).toFloat() // Invertimos Y (Norte arriba)

        // 5. Validamos si el dispositivo está dentro de los 100 metros
        val distanceFromCenter = Math.sqrt(xMeters * xMeters + yMeters * yMeters)

        val existingIcon = activeDevicesOnRadar[deviceName]

        if (distanceFromCenter > maxRadarRadiusMeters) {
            Log.d("GEOMETRIA_RADAR", "$deviceName está a ${distanceFromCenter.toInt()}m (Fuera de rango)")
            // Ocultamos el icono si se sale del radar
            existingIcon?.visibility = View.GONE
            return
        }

        Log.d("GEOMETRIA_RADAR", "Dibujando $deviceName a ${distanceFromCenter.toInt()}m -> X: $targetX px, Y: $targetY px")

        // 6. Dibujamos o actualizamos
        if (existingIcon != null) {
            // Aseguramos que sea visible si volvió a entrar al rango
            existingIcon.visibility = View.VISIBLE

            existingIcon.animate()
                .translationX(targetX)
                .translationY(targetY)
                .setDuration(500)
                .start()
        } else {
            val deviceIcon = ImageView(this).apply {
                setImageResource(R.drawable.dispositivoradar) // Tu icono
                contentDescription = deviceName

                setOnClickListener {
                    Toast.makeText(this@RadarActivity, "Usuario: $deviceName", Toast.LENGTH_SHORT).show()
                }
            }

            val iconSizePx = dpToPx(30f).toInt()
            val params = RelativeLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                // Esto ancla el icono al centro del radar
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }

            deviceIcon.layoutParams = params
            deviceIcon.translationX = targetX
            deviceIcon.translationY = targetY

            radarContainer.addView(deviceIcon)
            activeDevicesOnRadar[deviceName] = deviceIcon
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

    private fun sendWifiFrameOverNetwork() {
        info = WifiFrameUtils.buildMyWiFiFrame(this, userName, "RADAR")

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
            Log.e("TEST_ESTABILIDAD", "Faltan permisos")
            return
        }
        manager.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(applicationContext, "Ubicación enviada", Toast.LENGTH_SHORT).show()
                Log.d(TAG_WIFI, "tipo RADAR enviado en DNS-SD")
            }

            override fun onFailure(arg0: Int) {
                Log.e(TAG_WIFI, "Fallo al publicar mensaje: $arg0")
            }
        })
    }
}