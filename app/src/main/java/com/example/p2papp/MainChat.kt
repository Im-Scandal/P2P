package com.example.p2papp

import android.Manifest
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
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.p2papp.Constants.TAG
import com.example.p2papp.Constants.TAG_WIFI
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainChat : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    //Inicialización de variables de la vista
    private lateinit var op1Button: Button
    private lateinit var op2Button: Button
    private lateinit var op3Button: Button
    private lateinit var op4Button: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var userName: String

    //Selección de mensaje
    private var msg: String = ""

    //Variables para WiFi Direct
    private lateinit var wifiManager: WifiManager
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    //Variables para agregar los dispositivos (peers) al listview
    private var deviceArray: MutableList<MessageModel> = mutableListOf()
    private var info: WifiFrame = WifiFrame()
    private var selectedDevice: WifiP2pDevice? = null

    //Variables para agregar mensajes a recyclerView
    private var messages: MutableList<ChatMessage> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)

        initialWork()
        loadUserNameFromDatabase()

        addServiceRequest()
        startDiscover()


        messageAdapter = MessageAdapter(messages)

        recyclerView.adapter = messageAdapter
        exqListener()
    }

    private fun loadUserNameFromDatabase() {
        val db = AppDatabase.getDatabase(this) // Asumiendo que tienes un singleton de Room
        val userDao = db.userDao()
        CoroutineScope(Dispatchers.IO).launch {
            val savedUser = userDao.getUser()
            if (savedUser != null) {
                userName = savedUser.name
            }
        }
    }


    private fun exqListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@MainChat, MainMenu::class.java)
                startActivity(intent)
            }
        })
        op1Button.setOnClickListener {
            msg = op1Button.text.toString()

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

            saveMessage(msg)
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messages.size - 1)
            clearLocalServices {
                startRegistration()
            }
        }

        op2Button.setOnClickListener {
            msg = op2Button.text.toString()
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

            saveMessage(msg)
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messages.size - 1)
            clearLocalServices {
                startRegistration()
            }
        }

        op3Button.setOnClickListener {
            msg = op3Button.text.toString()
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

            saveMessage(msg)
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messages.size - 1)
            clearLocalServices {
                startRegistration()
            }
        }

        op4Button.setOnClickListener {
            msg = op4Button.text.toString()
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

            saveMessage(msg)
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messages.size - 1)
            clearLocalServices {
                startRegistration()
            }
        }

    }

    fun initialWork() {
        recyclerView = findViewById(R.id.messageRecyclerView)
//        listView = findViewById(R.id.peerListView)
        op1Button = findViewById(R.id.op1Button)
        op2Button = findViewById(R.id.op2Button)
        op3Button = findViewById(R.id.op3Button)
        op4Button = findViewById(R.id.op4Button)

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
                    Toast.makeText(this@MainChat, "success discover", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(code: Int) {
                    Toast.makeText(this@MainChat, "Failure discover", Toast.LENGTH_SHORT).show()
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


            //Toast.makeText(this, "Servicio encontrado : mensaje ${wifiFrame.sendMessage}" , Toast.LENGTH_SHORT ).show()
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
                    Toast.makeText(
                        this@MainChat,
                        "Success clear local services",
                        Toast.LENGTH_SHORT
                    ).show()
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
            deviceMulti.message.last().dateSend
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
        val newChatMessage = ChatMessage(
            nameUser = wifiFrame.nameUser,
            text = wifiFrame.sendMessage,
            timeSend = "Hora de envío: ${wifiFrame.dateSend}",
            timeReceived = "Hora de recepción: ${wifiFrame.dateReceived}",
            isSentByMe = false
        )
        runOnUiThread {
            messages.add(newChatMessage)
            messageAdapter.notifyItemInserted(messages.size - 1)
            recyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }


}



