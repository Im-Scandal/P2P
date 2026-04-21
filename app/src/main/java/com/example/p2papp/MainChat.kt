package com.example.p2papp

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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.p2papp.Constants.TAG
import com.example.p2papp.Constants.TAG_WIFI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math .atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import android.text.Editable
import android.text.TextWatcher
import android.graphics.Color


class MainChat : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    //Inicialización de variables de la vista
    private lateinit var op1Button: Button
    private lateinit var op2Button: Button
    private lateinit var op3Button: Button
    private lateinit var op4Button: Button
    private lateinit var ceButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var userName: String
    private lateinit var ceName: String
    private lateinit var cePhone: String
    private lateinit var aSalvoButton: Button
    private lateinit var puntosButton1: ImageButton
    private lateinit var puntosButton2: ImageButton
    private lateinit var tecladoButton: ImageButton
    private lateinit var messageEditText: EditText
    private lateinit var borrarButton: ImageButton
    private lateinit var charCounter: TextView

    private lateinit var dividerButtonChat: View
    private lateinit var linearLayoutTextoTresBtns: View
    private lateinit var botonesMsgs: View
    private lateinit var radarButton: ImageButton

    private var isSending = false

    //Variables para WiFi Direct
    private lateinit var wifiManager: WifiManager
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    //Variables para agregar los dispositivos (peers) al listview
    private var deviceArray: MutableList<MessageModel> = mutableListOf()
    private var info: WifiFrame = WifiFrame()

    //Variables para agregar mensajes a recyclerView
    private var messages: MutableList<ChatMessage> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initialWork()
        loadUserNameFromDatabase{
            addServiceRequest()
            startDiscover()
        }

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)


        messageAdapter = MessageAdapter(messages)

        recyclerView.adapter = messageAdapter
        exqListener()
    }

    fun initialWork() {
        radarButton = findViewById(R.id.radarButton)
        recyclerView = findViewById(R.id.messageRecyclerView)
        op1Button = findViewById(R.id.op1Button)
        op2Button = findViewById(R.id.op2Button)
        op3Button = findViewById(R.id.op3Button)
        op4Button = findViewById(R.id.op4Button)
        ceButton = findViewById(R.id.emergenciaButton)
        aSalvoButton = findViewById(R.id.estoyASalvoButton)
        messageEditText = findViewById(R.id.editTextText)
        borrarButton = findViewById(R.id.borrarButton)
        charCounter = findViewById(R.id.charCounter)


        dividerButtonChat = findViewById(R.id.dividerBotonesChat)
        linearLayoutTextoTresBtns = findViewById(R.id.linearLayoutTextoTresBtns)
        botonesMsgs = findViewById(R.id.botonesMsgs)

        puntosButton1 = findViewById(R.id.trespuntosButton1)
        puntosButton2 = findViewById(R.id.trespuntosButton2)
        tecladoButton = findViewById(R.id.tecladoButton)

        wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        WifiFrameUtils.getUUIDWiFiFrame(this)

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No necesitamos hacer nada antes de que cambie
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Obtenemos la longitud actual
                val currentLength = s?.length ?: 0

                // Actualizamos el texto del contador
                charCounter.text = "$currentLength/200"

                // Pista visual de advertencia: se pone rojo al llegar a 200
                if (currentLength >= 200) {
                    charCounter.setTextColor(Color.RED)
                } else {
                    charCounter.setTextColor(Color.WHITE)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun loadUserNameFromDatabase(onLoaded: () -> Unit) {
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            val savedUser = userDao.getUser()
            if (savedUser != null) {
                userName = savedUser.name.trim()
                ceName = savedUser.nameCE
                cePhone = savedUser.phoneCE
            }

            val savedMessages = db.chatMessageDao().getAllMessages()
            val chatMessages = savedMessages.map {
                ChatMessage(
                    nameUser = it.nameUser,
                    text = it.text,
                    timeSend = it.timeSend,
                    timeReceived = it.timeReceived,
                    isSentByMe = it.isSentByMe
                )
            }

            runOnUiThread {
                messages.addAll(chatMessages)
                messageAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
                onLoaded()
            }
        }
    }

    private fun exqListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val overlayView = LayoutInflater.from(this@MainChat).inflate(R.layout.confirmacion_chat, null)
                val texto = overlayView.findViewById<TextView>(R.id.texto)
                texto.text = getString(R.string.salir_chat)
                val rootView = findViewById<ViewGroup>(android.R.id.content)
                rootView.addView(overlayView)

                val closeApp = overlayView.findViewById<Button>(R.id.siButton)
                closeApp.setOnClickListener{
                    rootView.removeView(overlayView)
                    val intent = Intent(this@MainChat, MainMenu::class.java)
                    startActivity(intent)
                }

                val closeButton = overlayView.findViewById<Button>(R.id.noButton)
                closeButton.setOnClickListener {
                    rootView.removeView(overlayView)
                }
            }
        })
        aSalvoButton.setOnClickListener{
            val overlayView = LayoutInflater.from(this@MainChat).inflate(R.layout.confirmacion_chat, null)
            val texto = overlayView.findViewById<TextView>(R.id.texto)
            texto.text = getString(R.string.salir_chat)
            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(overlayView)

            val closeApp = overlayView.findViewById<Button>(R.id.siButton)
            closeApp.setOnClickListener{
                rootView.removeView(overlayView)
                val intent = Intent(this, MainMenu::class.java)
                startActivity(intent)
            }

            val closeButton = overlayView.findViewById<Button>(R.id.noButton)
            closeButton.setOnClickListener {
                rootView.removeView(overlayView)
            }
        }
        radarButton.setOnClickListener {
            val intent = Intent(this, RadarActivity::class.java)
            startActivity(intent)
        }

        op1Button.setOnClickListener { sendMessageWithLocation(op1Button.text.toString(), op1Button) }
        op2Button.setOnClickListener { sendMessageWithLocation(op2Button.text.toString(), op2Button) }
        op3Button.setOnClickListener { sendMessageWithLocation(op3Button.text.toString(), op3Button) }
        op4Button.setOnClickListener { sendMessageWithLocation(op4Button.text.toString(), op4Button) }

        ceButton.setOnClickListener {
            if (ceName.isEmpty() || cePhone.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Necesitas configurar tu\ncontacto de emergencia",
                    Toast.LENGTH_LONG
                ).show()

            }else {
                val msg = "Contacto de emergencia: $ceName\nTeléfono: $cePhone"
                sendMessageWithLocation(msg, ceButton)
            }
        }

        puntosButton1.setOnClickListener {
            tecladoButton.visibility = if (tecladoButton.isVisible){
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        puntosButton2.setOnClickListener {
            tecladoButton.visibility = if (tecladoButton.isVisible){
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        tecladoButton.setOnClickListener{
            if (botonesMsgs.isVisible) {
                dividerButtonChat.visibility = View.GONE
                linearLayoutTextoTresBtns.visibility = View.GONE
                botonesMsgs.visibility = View.GONE
                messageEditText.visibility = View.VISIBLE
                puntosButton2.visibility = View.VISIBLE
                charCounter.visibility = View.VISIBLE
                aSalvoButton.setBottomMargin(12)
                tecladoButton.setImageResource(R.drawable.group_72)

                messageEditText.requestFocus()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT)
            }else{
                dividerButtonChat.visibility = View.VISIBLE
                linearLayoutTextoTresBtns.visibility = View.VISIBLE
                botonesMsgs.visibility = View.VISIBLE
                messageEditText.visibility = View.GONE
                puntosButton2.visibility = View.GONE
                charCounter.visibility = View.GONE
                aSalvoButton.setBottomMargin(50)
                tecladoButton.setImageResource(R.drawable.group_72__1_)

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager  // Oculta el teclado
                imm.hideSoftInputFromWindow(messageEditText.windowToken, 0)
            }
        }

        messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val msg = messageEditText.text.toString()
                if (msg.isNotBlank()) {
                    sendMessageWithLocation(msg, null)
                    messageEditText.text.clear()
                }
                true
            } else {
                false
            }
        }

        borrarButton.setOnClickListener { confirmDeleteMessages() }

    }

    private fun sendMessageWithLocation(msg: String, button: Button? = null) {
        if (isSending) {
            Toast.makeText(this, "Espera antes de enviar otro mensaje", Toast.LENGTH_SHORT).show()
            return
        }

        isSending = true
        button?.background = ContextCompat.getDrawable(this, R.drawable.boton_pressed)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Verificación de permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val lastLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (lastLocation != null) {
                executeSend(msg, lastLocation.latitude, lastLocation.longitude, button)
            } else {
                // Intento secundario si el caché de GPS está vacío
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    locationManager.getCurrentLocation(
                        LocationManager.GPS_PROVIDER, null, mainExecutor
                    ) { location ->
                        if (location != null) {
                            executeSend(msg, location.latitude, location.longitude, button)
                        } else {
                            executeSend(msg, null, null, button) // Fallback sin ubicación
                        }
                    }
                } else {
                    executeSend(msg, null, null, button) // Fallback sin ubicación
                }
            }
        } else {
            executeSend(msg, null, null, button) // Envío normal si no hay permisos
        }
    }

    private fun executeSend(msg: String, lat: Double?, lon: Double?, button: Button?) {

        Log.e(TAG_WIFI, "latitud: $lat, longitud: $lon")
        // 1. Guardar en SharedPreferences (Para que buildMyWiFiFrame lo lea)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.MESSAGE, msg.trim())
        if (lat != null) editor.putString("LATITUDE", lat.toString()) else editor.remove("LATITUDE")
        if (lon != null) editor.putString("LONGITUDE", lon.toString()) else editor.remove("LONGITUDE")
        editor.apply()

        // 2. Actualizar UI
        val newMsg = ChatMessage(
            nameUser = userName,
            text = msg.trim(),
            timeSend = "Hora de envío: " + getFormattedDateTime(),
            timeReceived = "",
            isSentByMe = true,
            latitude = lat,
            longitude = lon
        )

        runOnUiThread {
            messages.add(newMsg)
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messages.size - 1)
            Toast.makeText(applicationContext, "Mensaje enviado", Toast.LENGTH_SHORT).show()
        }

        // 3. Guardar en Room
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.chatMessageDao().insertMessage(
                ChatMessageEntity(
                    nameUser = userName,
                    text = msg.trim(),
                    timeSend = "Hora de envío: " + getFormattedDateTime(),
                    timeReceived = "",
                    isSentByMe = true,
                    latitude = lat,
                    longitude = lon
                )
            )
        }

        // 4. Reiniciar servicio WiFi Direct
        clearLocalServices {
            startRegistration()
        }

        // 5. Restaurar botón
        Handler(Looper.getMainLooper()).postDelayed({
            isSending = false
            button?.background = ContextCompat.getDrawable(this@MainChat, R.drawable.boton_respuestas)
        }, 5000)
    }

    fun View.setBottomMargin(marginInDp: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        val scale = resources.displayMetrics.density
        params.bottomMargin = (marginInDp * scale + 0.5f).toInt()
        layoutParams = params
    }

    private fun startRegistration() {

        info = WifiFrameUtils.buildMyWiFiFrame(this, userName)

        val record = WifiFrameUtils.wifiFrameToHashMap(info)

        val payloadSize = calculatePayloadSizeBytes(record)

        // Log de depuración (Debug) para registrar cada envío
        Log.d("TEST_ESTABILIDAD", "Enviando paquete... Tamaño de carga útil: $payloadSize bytes")

        // Validación del límite estricto de 255 bytes
        if (payloadSize > 255) {
            Log.e("TEST_ESTABILIDAD", "¡ERROR DE CASO DE BORDE! El paquete superó el límite: $payloadSize bytes")
        } else {
            Log.i("TEST_ESTABILIDAD", "Éxito: El paquete cumple con el protocolo de red.")
        }


        // Service information.  Pass it an instance name, service type
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

            }

            override fun onFailure(arg0: Int) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Toast.makeText(this@MainChat, "Fail local service", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addServiceRequest() {
        val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(
            "_networkChat",
            "_chatApp._tcp"
        )

        manager.addServiceRequest(
            channel,
            serviceRequest,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    discoverListener()
                }

                override fun onFailure(code: Int) {
                    Toast.makeText(this@MainChat, "Failure addService", Toast.LENGTH_SHORT)
                        .show()
                    Log.e(TAG_WIFI, "Add service request has failed. $code")
                }
            }
        )
    }

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var task: Runnable? = null
    private var interval: Long = 10000
    private val devicesWithReceivedMessages: MutableSet<String> = mutableSetOf()


    fun startDiscover() {
        // Crea un nuevo Runnable que se ejecutará después de cada intervalo
        task = Runnable {
            discoverServices()
            clearReceivedDevicesAfterDelay()

        }
        // Programa el primer ciclo del temporizador con un retraso inicial de 0 y un intervalo especificado
        executor.scheduleWithFixedDelay(task!!, 0, interval, TimeUnit.MILLISECONDS)
    }

    private fun discoverServices() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT > Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            Toast.makeText(this, "Faltan pemisos 4", Toast.LENGTH_SHORT).show()
            return
        }
        else { manager.discoverServices(
            channel,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
//                    Toast.makeText(this@MainChat, "success discover", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(code: Int) {
//                    Toast.makeText(this@MainChat, "Failure discover", Toast.LENGTH_SHORT).show()
                    Log.e(TAG_WIFI, "Discover services has failed. $code")
                }
            }
        )}
    }

    private fun discoverListener() {

        val txtListener = WifiP2pManager.DnsSdTxtRecordListener { fullDomain, record, srcDevice ->
            Log.d(TAG, "DnsSdTxtRecord available -$record")
            val wifiFrame = WifiFrameUtils.hashMapToWiFiFrame(record)
            if (record.isEmpty() || srcDevice.deviceName == "" || WifiFrameUtils.deviceIdMultiHop == WifiFrameUtils.idDevice || wifiFrame.sendMessage.isEmpty()) return@DnsSdTxtRecordListener

            if (WifiFrameUtils.deviceMultihop.isNotEmpty()) {
                val deviceP2p = WifiP2pDevice().apply {
                    deviceName = WifiFrameUtils.deviceMultihop
                }
                wifiFrame.apply {
                    nameMultiHop = srcDevice.deviceName
                }
                addDeviceMultiHop(deviceP2p, wifiFrame)
            } else {
                addDeviceList(srcDevice, wifiFrame)
            }

        }

        val servListener =
            WifiP2pManager.DnsSdServiceResponseListener { instanceName, registrationType, resourceType ->
                Log.d("chat", "BonjourService available! instanceName: $instanceName")
                Log.d("chat", "BonjourService available! registrationType: $registrationType")
                Log.d("chat", "BonjourService available! resourceType: $resourceType")
            }


        manager.setDnsSdResponseListeners(channel, servListener, txtListener)
    }

    private fun clearLocalServices(onSuccessCallback: () -> Unit) {
        manager.clearLocalServices(channel,
            object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.d("success", "clearLocalServices result: Success")
//                    Toast.makeText(
//                        this@MainChat,
//                        "Success clear local services",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    onSuccessCallback.invoke()
                }

                override fun onFailure(code: Int) {
                    Log.e("Failed", "clearLocalServices result:  Failure with code $code")
                    Toast.makeText(
                        this@MainChat,
                        "Failed to clear local services: $code",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun clearReceivedDevicesAfterDelay() {
        // Limpia el conjunto de dispositivos después de cierto tiempo
        executor.schedule({
            devicesWithReceivedMessages.clear()
        }, 500, TimeUnit.MILLISECONDS)
    }

    private fun addDeviceList(record: WifiP2pDevice, wifiFrame: WifiFrame) {

        val deviceSame = deviceArray.firstOrNull { it.device.deviceName == record.deviceName }

        if (deviceSame != null) {
            val messageExist = deviceSame.message.any {
                it.dateSend == wifiFrame.dateSend
            }

            if (!messageExist){
                deviceSame.message.add(wifiFrame)
                onWifiFrameReceived(wifiFrame)}

        } else {
            val message = MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)
            deviceArray.add(message)

            onWifiFrameReceived(wifiFrame)
        }

        multihop(WifiFrameUtils.deviceIdMultiHop)
    }

    private fun multihop(deviceId: String) {

        val deviceMulti = deviceArray.first {
            it.id == deviceId
        }

        val record = WifiFrameUtils.wifiFrameToHashMapMultihop(
            deviceMulti.device,
            deviceMulti.message.last().sendMessage,
            deviceMulti.id,
            deviceMulti.message.last().dateSend,
            deviceMulti.message.last().nameUser // Aquí pasas el nombre del emisor original
        )


        val payloadSize = calculatePayloadSizeBytes(record)

        // Log de depuración (Debug) para registrar cada envío
        Log.d("TEST_ESTABILIDAD", "RE enviando paquete... Tamaño de carga útil: $payloadSize bytes")

        // Validación del límite estricto de 255 bytes
        if (payloadSize > 255) {
            Log.e("TEST_ESTABILIDAD", "¡ERROR DE CASO DE BORDE! El paquete superó el límite: $payloadSize bytes")
        } else {
            Log.i("TEST_ESTABILIDAD", "Éxito: El paquete cumple con el protocolo de red.")
        }

        // Service information.  Pass it an instance name, service type
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

            }

            override fun onFailure(arg0: Int) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Toast.makeText(this@MainChat, "Fail local service", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addDeviceMultiHop(record: WifiP2pDevice, wifiFrame: WifiFrame) {

        val deviceSame = deviceArray.firstOrNull { it.device.deviceName == record.deviceName }
        if (deviceSame != null) {
            val messageExist = deviceSame.message.any {
                it.dateSend == wifiFrame.dateSend
            }

            if (!messageExist) {
                deviceSame.message.add(wifiFrame)
                onWifiFrameReceived(wifiFrame)
            }

        } else {
            val device = MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)
            deviceArray.add(device)

            onWifiFrameReceived(wifiFrame)
        }

    }

    fun onWifiFrameReceived(wifiFrame: WifiFrame) {
        if (wifiFrame.type == "CHAT") {
            // Es un mensaje normal. Lo muestras en pantalla y se guarda en Room.
            addMessageToRecyclerView(wifiFrame)

        } else if (wifiFrame.type == "RADAR") {
            // Es un ping de ubicación.
            //updateRadarWithNewLocation(wifiFrame)
        }
    }

    private fun addMessageToRecyclerView(wifiFrame: WifiFrame) {
        val enviadoPorMi = wifiFrame.nameUser == userName

        // 1. Obtener mi ubicación actual solo para el cálculo inicial
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val myLocation = if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else null

        // 2. Calcular distancia con validación de umbral (Opción A)
        val distanceResult: String = if (enviadoPorMi) {
            "" // No calculamos distancia para nuestros propios mensajes
        } else if (myLocation != null && wifiFrame.latitude != null && wifiFrame.longitude != null) {

            val d = calculateDistance(myLocation.latitude, myLocation.longitude, wifiFrame.latitude!!, wifiFrame.longitude!!)
            val distanceInMeters = (d * 1000).toInt()

            // Lógica de Umbral: Si está a menos de 50m, consideramos que está en el mismo lugar
            when {
                distanceInMeters < 5 -> "Muy cerca $distanceInMeters"
                distanceInMeters < 10 -> "Cerca $distanceInMeters"
                distanceInMeters < 1000 -> "$distanceInMeters m"
                else -> "%.2f km".format(d)
            }

        } else {
            // Manejo de errores de sensores
            if (myLocation == null) "Tu GPS sin señal"
            else "Ubicación remota desconocida"
        }

        val newChatMessage = ChatMessage(
            nameUser = wifiFrame.nameUser,
            text = wifiFrame.sendMessage,
            timeSend = "Hora de envío: ${wifiFrame.dateSend}",
            timeReceived = "Hora de recepción: ${wifiFrame.dateReceived}",
            distance = "Distancia: $distanceResult",
            isSentByMe = enviadoPorMi
        )

        runOnUiThread {
            messages.add(newChatMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.smoothScrollToPosition(messages.size - 1)

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(applicationContext)
                db.chatMessageDao().insertMessage(
                    ChatMessageEntity(
                        nameUser = wifiFrame.nameUser,
                        text = wifiFrame.sendMessage,
                        timeSend = "Hora enviada: ${wifiFrame.dateSend}",
                        timeReceived = "Hora recibido: ${wifiFrame.dateReceived}",
                        isSentByMe = enviadoPorMi,
                        latitude = wifiFrame.latitude,
                        longitude = wifiFrame.longitude,
                        distance = "Distancia: ${wifiFrame.distance}"
                    )
                )
            }
        }
    }

    private fun confirmDeleteMessages() {
        val inflater = LayoutInflater.from(this)
        val overlayView = inflater.inflate(R.layout.delete_warning, null)

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(overlayView)

        val btnConfirm = overlayView.findViewById<Button>(R.id.ButtonBorrar)
        val btnCancel = overlayView.findViewById<Button>(R.id.ButtonCancelar)

        btnConfirm.setOnClickListener {
            executeDeleteAll()
            rootView.removeView(overlayView)
        }

        btnCancel.setOnClickListener {
            rootView.removeView(overlayView)
        }
    }

    private fun executeDeleteAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.chatMessageDao().clearMessages()

            runOnUiThread {
                messages.clear()
                messageAdapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, "Historial borrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Formula de Harvesine
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en kilómetros

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c // Devuelve la distancia en kilómetros
    }

    // Retorna un Pair donde el primer valor es X (Este/Oeste) y el segundo es Y (Norte/Sur) en metros
    fun getRelativeCartesian(myLat: Double, myLon: Double, targetLat: Double, targetLon: Double): Pair<Double, Double> {
        val earthRadius = 6371000.0 // Radio de la Tierra en metros

        // Convertir diferencias a radianes
        val dLat = Math.toRadians(targetLat - myLat)
        val dLon = Math.toRadians(targetLon - myLon)
        val myLatRad = Math.toRadians(myLat)

        // Cálculo de X e Y en metros
        val x = earthRadius * dLon * cos(myLatRad)
        val y = earthRadius * dLat

        return Pair(x, y)
    }

    private fun calculatePayloadSizeBytes(record: HashMap<String, String>): Int {
        var totalBytes = 0
        Log.d("TEST_ESTABILIDAD", "--- Inicio de Desglose de Paquete DNS-SD ---")

        for ((key, value) in record) {
            val entryString = "$key=$value"
            val entrySize = entryString.toByteArray(Charsets.UTF_8).size

            // 1. Log del peso individual de cada campo
            Log.d("TEST_ESTABILIDAD", "Campo: [$key] -> $entrySize bytes")

            // 2. Validación estricta individual de cada par
            if (entrySize > 255) {
                Log.e("TEST_ESTABILIDAD", "¡ERROR! El campo [$key] tiene $entrySize bytes")
            }

            // Suma total del paquete
            totalBytes += entrySize
        }

        Log.d("TEST_ESTABILIDAD", "--- Fin de Desglose ---")
        return totalBytes
    }

}



