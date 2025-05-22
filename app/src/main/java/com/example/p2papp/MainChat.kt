package com.example.p2papp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import androidx.recyclerview.widget.RecyclerView
import com.example.p2papp.Constants.TAG
import com.example.p2papp.Constants.TAG_WIFI
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import androidx.core.view.isVisible
import androidx.core.view.marginBottom


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

    private lateinit var dividerButtonChat: View
    private lateinit var linearLayoutTextoTresBtns: View
    private lateinit var botonesMsgs: View

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
        op1Button.setOnClickListener {
            sendMessageButtons(op1Button)
        }

        op2Button.setOnClickListener {
            sendMessageButtons(op2Button)
        }

        op3Button.setOnClickListener {
            sendMessageButtons(op3Button)
        }

        op4Button.setOnClickListener {
            sendMessageButtons(op4Button)
        }

        ceButton.setOnClickListener {
            if (ceName.isEmpty() || cePhone.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Necesitas configurar tu\ncontacto de emergencia",
                    Toast.LENGTH_LONG
                ).show()

            }else {
                if (isSending) {
                    Toast.makeText(this, "Espera antes de enviar otro mensaje", Toast.LENGTH_SHORT).show()
                }else{

                isSending = true
                ceButton.background = ContextCompat.getDrawable(this, R.drawable.boton_pressed)

                val msg = "Contacto de emergencia: $ceName\nTeléfono: $cePhone"

                Toast.makeText(
                    applicationContext,
                    "Mensaje enviado",
                    Toast.LENGTH_LONG
                ).show()

                messages.add(
                    ChatMessage(
                        nameUser = userName,
                        text = msg.trim(),
                        timeSend = "Hora de envío: " + getFormattedDateTime(),
                        timeReceived = "",
                        isSentByMe = true
                    )
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.chatMessageDao().insertMessage(
                        ChatMessageEntity(
                            nameUser = userName,
                            text = msg.trim(),
                            timeSend = "Hora de envío: " + getFormattedDateTime(),
                            timeReceived = "",
                            isSentByMe = true
                        )
                    )
                }

                saveMessage(msg)
                messageAdapter.notifyDataSetChanged()
                recyclerView.smoothScrollToPosition(messages.size - 1)
                clearLocalServices {
                    startRegistration()
                }

                ceButton.postDelayed({
                    isSending = false
                    ceButton.background = ContextCompat.getDrawable(this, R.drawable.boton_respuestas) // Restaurar color original
                }, 5000)
            }}
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
                aSalvoButton.setBottomMargin(12)
                tecladoButton.setImageResource(R.drawable.group_72)

                messageEditText.requestFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT)
            }else{
                dividerButtonChat.visibility = View.VISIBLE
                linearLayoutTextoTresBtns.visibility = View.VISIBLE
                botonesMsgs.visibility = View.VISIBLE
                messageEditText.visibility = View.GONE
                puntosButton2.visibility = View.GONE
                aSalvoButton.setBottomMargin(50)
                tecladoButton.setImageResource(R.drawable.group_72__1_)

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager  // Oculta el teclado
                imm.hideSoftInputFromWindow(messageEditText.windowToken, 0)
            }
        }

        messageEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendMessageEditText()
                true
            } else {
                false
            }
        }

    }

    private fun sendMessageButtons(opButton: Button) {
        if (isSending) {
            Toast.makeText(this, "Espera antes de enviar otro mensaje", Toast.LENGTH_SHORT).show()
            return
        }

        isSending = true
        opButton.background = ContextCompat.getDrawable(this, R.drawable.boton_pressed)

        val msg = opButton.text.toString()

        Toast.makeText(
            applicationContext,
            "Mensaje enviado",
            Toast.LENGTH_LONG
        ).show()

        messages.add(ChatMessage(
            nameUser = userName,
            text = msg.trim(),
            timeSend = "Hora de envío: " + getFormattedDateTime(),
            timeReceived = "",
            isSentByMe = true
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.chatMessageDao().insertMessage(
                ChatMessageEntity(
                    nameUser = userName,
                    text = msg.trim(),
                    timeSend = "Hora de envío: " + getFormattedDateTime(),
                    timeReceived = "",
                    isSentByMe = true
                )
            )
        }

        saveMessage(msg)
        messageAdapter.notifyDataSetChanged()
        recyclerView.smoothScrollToPosition(messages.size - 1)
        clearLocalServices {
            startRegistration()
        }
        // Espera de 5 segundos antes de volver a permitir enviar
        opButton.postDelayed({
            isSending = false
            opButton.background = ContextCompat.getDrawable(this, R.drawable.boton_respuestas) // Restaurar color original
        }, 5000)
    }

    private fun sendMessageEditText() {
        val msg = messageEditText.text.toString()
        if (isSending) {
            Toast.makeText(this, "Espera antes de enviar otro mensaje", Toast.LENGTH_SHORT).show()
            return
        }else{
            if (msg.isBlank()) {
                Toast.makeText(applicationContext, "Escribe un mensaje", Toast.LENGTH_LONG).show()
            } else {
                isSending = true
                messageEditText.text.clear()
                Toast.makeText(applicationContext, "Mensaje enviado", Toast.LENGTH_LONG).show()

                messages.add(
                    ChatMessage(
                        nameUser = userName,
                        text = msg.trim(),
                        timeSend = "Hora de envío: " + getFormattedDateTime(),
                        timeReceived = "",
                        isSentByMe = true
                    )
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.chatMessageDao().insertMessage(
                        ChatMessageEntity(
                            nameUser = userName,
                            text = msg.trim(),
                            timeSend = "Hora de envío: " + getFormattedDateTime(),
                            timeReceived = "",
                            isSentByMe = true
                        )
                    )
                }

                saveMessage(msg)
                messageAdapter.notifyDataSetChanged()
                recyclerView.smoothScrollToPosition(messages.size - 1)
                clearLocalServices {
                    startRegistration()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    isSending = false
                }, 5000)
            }
        }
    }

    fun View.setBottomMargin(marginInDp: Int) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        val scale = resources.displayMetrics.density
        params.bottomMargin = (marginInDp * scale + 0.5f).toInt()
        layoutParams = params
    }

    fun initialWork() {
        recyclerView = findViewById(R.id.messageRecyclerView)
        op1Button = findViewById(R.id.op1Button)
        op2Button = findViewById(R.id.op2Button)
        op3Button = findViewById(R.id.op3Button)
        op4Button = findViewById(R.id.op4Button)
        ceButton = findViewById(R.id.emergenciaButton)
        aSalvoButton = findViewById(R.id.estoyASalvoButton)
        messageEditText = findViewById(R.id.editTextText)

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
    }

    private fun startRegistration() {

        info = WifiFrameUtils.buildMyWiFiFrame(this, userName)

        val record = WifiFrameUtils.wifiFrameToHashMap(info)

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

    private fun saveMessage(message: String) {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        editor.putString(Constants.MESSAGE, message.trim())
        // Guardar la lista actualizada en SharedPreferences
        editor.putString(Constants.MESSAGE_LIST, gson.toJson(messages))
        editor.apply()
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
        // Limpia el conjunto de dispositivos después de cierto tiempo (por ejemplo, 30 segundos)
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
                addMessageToRecyclerView(wifiFrame)}

        } else {
            val message = MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)
            deviceArray.add(message)

            addMessageToRecyclerView(wifiFrame)
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
                addMessageToRecyclerView(wifiFrame)
            }

        } else {
            val device = MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)
            deviceArray.add(device)

            addMessageToRecyclerView(wifiFrame)
        }

    }

    private fun addMessageToRecyclerView(wifiFrame: WifiFrame) {
        val enviadoPorMi = wifiFrame.nameUser == userName

        val newChatMessage = ChatMessage(
            nameUser = wifiFrame.nameUser,
            text = wifiFrame.sendMessage,
            timeSend = "Hora de envío: ${wifiFrame.dateSend}",
            timeReceived = "Hora de recepción: ${wifiFrame.dateReceived}",
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
                        isSentByMe = enviadoPorMi
                    )
                )
            }
        }
    }



}



