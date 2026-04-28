package com.example.p2papp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
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
import android.widget.TextView
import android.widget.Toast
import android.widget.ZoomButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.p2papp.Constants.TAG_WIFI
import com.example.p2papp.NetworkManager.Companion.lastBestLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import kotlin.math.cos

class RadarActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var userName: String = "Usuario" // Valor por defecto
    private var radarJob: Job? = null

    private lateinit var homeButton: ImageButton
    private lateinit var helpButton: Button
    private lateinit var perfilButton: ImageButton
    private lateinit var bibliotecaButton: ImageButton
    private lateinit var lessZoomButton: ImageButton
    private lateinit var moreZoomButton: ImageButton
    private lateinit var textZoom: TextView
    private lateinit var radarContainer: RelativeLayout // Contenedor del radar
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    var maxRadarRadiusMeters = 100

    data class RadarDevice(
        val icon: ImageView,
        var xMeters: Double,
        var yMeters: Double,
        var distancia: Int
    )
    private val activeDevicesOnRadar = mutableMapOf<String, RadarDevice>()

    private lateinit var overlayManager: OverlayManager
    private var info: WifiFrame = WifiFrame()
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel


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

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

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
        NetworkManager.addServiceRequest(this)
        NetworkManager.startDiscover(this)
        startGpsTracking()
        rotationSensor?.let {
            // SENSOR_DELAY_UI es un buen equilibrio entre fluidez y consumo de batería
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        radarJob?.cancel()
        sensorManager.unregisterListener(sensorEventListener)
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
                if (lastBestLocation != null) {
                    val myLat = lastBestLocation!!.latitude
                    val myLon = lastBestLocation!!.longitude

                    val editor = sharedPreferences.edit()
                    editor.putString(Constants.MESSAGE, "RADAR")
                    editor.putString("LATITUDE", myLat.toString())
                    editor.putString("LONGITUDE", myLon.toString())
                    editor.apply()

                    Log.d(TAG_WIFI, "Mi latitud: $myLat, longitud: $myLon")

                    sendWifiFrameOverNetwork()
                } else {
                    Log.e(TAG_WIFI, "No se pudo obtener la ubicación")
                }

                // Intervalo de 60 segundos
                delay(60000)
            }
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
                val distanciaMts = NetworkManager.calculateHaversine(targetLat, targetLon, this)

                // 2. Dibujar en la interfaz
                runOnUiThread {
                    addOrUpdateDeviceOnRadar(xMeters, yMeters, distanciaMts, deviceName)
                }
            }
        }
    }

    private fun addOrUpdateDeviceOnRadar(xMeters: Double, yMeters: Double, distancia: Int, deviceName: String) {

        val containerWidth = radarContainer.width
        val containerHeight = radarContainer.height

        if (containerWidth == 0 || containerHeight == 0) return

        val containerRadiusPx = (Math.min(containerWidth, containerHeight) / 2f).toDouble()
        val pixelsPerMeter = containerRadiusPx / maxRadarRadiusMeters

        val targetX = (xMeters * pixelsPerMeter).toFloat()
        val targetY = (-yMeters * pixelsPerMeter).toFloat()

        val distanceFromCenter = Math.sqrt(xMeters * xMeters + yMeters * yMeters)

        val existingDevice = activeDevicesOnRadar[deviceName]

        if (distanceFromCenter > maxRadarRadiusMeters) {
            existingDevice?.icon?.visibility = View.GONE
            return
        }

        if (existingDevice != null) {
            // 1. Actualizamos la memoria física por si el usuario hizo zoom o caminó
            existingDevice.xMeters = xMeters
            existingDevice.yMeters = yMeters
            existingDevice.distancia = distancia

            // 2. Actualizamos el clic para que el Toast muestre la nueva distancia
            existingDevice.icon.setOnClickListener {
                Toast.makeText(this@RadarActivity, "Usuario: $deviceName - Distancia: ${existingDevice.distancia} mts.", Toast.LENGTH_SHORT).show()
            }

            // 3. Movemos el icono
            existingDevice.icon.visibility = View.VISIBLE
            existingDevice.icon.animate()
                .translationX(targetX)
                .translationY(targetY)
                .setDuration(500)
                .start()
        } else {
            // Creamos la imagen por primera vez
            val deviceIcon = ImageView(this).apply {
                setImageResource(R.drawable.dispositivoradar)
                contentDescription = deviceName
            }

            val iconSizePx = dpToPx(30f).toInt()
            val params = RelativeLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }

            deviceIcon.layoutParams = params
            deviceIcon.translationX = targetX
            deviceIcon.translationY = targetY

            // Al crearlo por primera vez, referenciamos la variable temporal 'distancia'
            deviceIcon.setOnClickListener {
                Toast.makeText(this@RadarActivity, "Usuario: $deviceName - Distancia: ${activeDevicesOnRadar[deviceName]?.distancia ?: distancia} mts.", Toast.LENGTH_SHORT).show()
            }

            radarContainer.addView(deviceIcon)

            // Lo guardamos en el mapa usando nuestro nuevo paquete de datos
            activeDevicesOnRadar[deviceName] = RadarDevice(deviceIcon, xMeters, yMeters, distancia)
        }
    }

    // Funciones de la brujula (Compass)

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

                val orientationValues = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                val azimuthInRadians = orientationValues[0].toDouble()
                val azimuthInDegrees = Math.toDegrees(azimuthInRadians).toFloat()

                radarContainer.rotation = -azimuthInDegrees

                for (device in activeDevicesOnRadar.values) {
                    device.icon.rotation = azimuthInDegrees
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

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
        lessZoomButton = findViewById(R.id.lessZoom)
        moreZoomButton = findViewById(R.id.moreZoom)
        textZoom = findViewById(R.id.textZoom)
        radarContainer = findViewById(R.id.radarContainer)
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        textZoom.text = "${maxRadarRadiusMeters.toInt()}m"
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

        lessZoomButton.setOnClickListener {
            when (maxRadarRadiusMeters.toInt()) {
                50 -> Toast.makeText(this, "Zoom mínimo alcanzado", Toast.LENGTH_SHORT).show()
                100 -> {
                    maxRadarRadiusMeters = 50
                    textZoom.text = "${maxRadarRadiusMeters.toInt()}m"
                    refreshRadarScale() // <-- Esta es la función que crearemos ahora
                }
                in 101..500 -> {
                    maxRadarRadiusMeters -= 100
                    textZoom.text = "${maxRadarRadiusMeters.toInt()}m"
                    refreshRadarScale()
                }
            }
        }

        moreZoomButton.setOnClickListener {
            when (maxRadarRadiusMeters.toInt()) {
                500 -> Toast.makeText(this, "Zoom máximo alcanzado", Toast.LENGTH_SHORT).show()
                50 -> {
                    maxRadarRadiusMeters = 100
                    textZoom.text = "${maxRadarRadiusMeters.toInt()}m"
                    refreshRadarScale()
                }
                in 100..499 -> {
                    maxRadarRadiusMeters += 100
                    textZoom.text = "${maxRadarRadiusMeters.toInt()}m"
                    refreshRadarScale()
                }
            }
        }
    }

    private fun refreshRadarScale() {
        val containerWidth = radarContainer.width
        val containerHeight = radarContainer.height

        if (containerWidth == 0 || containerHeight == 0) return

        val containerRadiusPx = (Math.min(containerWidth, containerHeight) / 2f).toDouble()

        // 1. Calculo de la nueva escala en base al zoom
        val pixelsPerMeter = containerRadiusPx / maxRadarRadiusMeters

        // 2. Reposición de todos los puntos en el radar
        for ((_, device) in activeDevicesOnRadar) {
            val targetX = (device.xMeters * pixelsPerMeter).toFloat()
            val targetY = (-device.yMeters * pixelsPerMeter).toFloat()

            val distanceFromCenter = Math.sqrt(device.xMeters * device.xMeters + device.yMeters * device.yMeters)

            // 3. Ocultamos los que queden fuera del nuevo límite, o animamos los que estén dentro
            if (distanceFromCenter > maxRadarRadiusMeters) {
                device.icon.visibility = View.GONE
            } else {
                device.icon.visibility = View.VISIBLE
                device.icon.animate()
                    .translationX(targetX)
                    .translationY(targetY)
                    .setDuration(300)
                    .start()
            }
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