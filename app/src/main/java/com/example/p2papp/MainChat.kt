package com.example.p2papp

import android.Manifest
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
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.p2papp.Constants.TAG
import com.example.p2papp.Constants.TAG_WIFI
import com.example.wifidiscover.MessageAdapter
import com.google.gson.Gson
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainChat : AppCompatActivity()
{
    private lateinit var sharedPreferences: SharedPreferences

    //Inicialización de variables de la vista
    private lateinit var btnDiscover: Button
    private lateinit var op1Button: Button
    private lateinit var op2Button: Button
    private lateinit var op3Button: Button
    private lateinit var listView: ListView
    lateinit var msgSend: TextView
    lateinit var connectionStatus: TextView
    private lateinit var recyclerView: RecyclerView
    var userName: String = SolicitedName.nameUser

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
    private var messages: MutableList<String> = mutableListOf()
    private lateinit var messageAdapter: MessageAdapter

    lateinit var nameUser: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences(Constants.PREFERENCES_KEY, MODE_PRIVATE)

        initialWork()
        addServiceRequest()
        startDiscover()

        connectionStatus.setText(userName)

        messageAdapter = MessageAdapter(messages)

        recyclerView.adapter = messageAdapter
        exqListener()
        listView.setOnItemClickListener { parent, viiew, pos, id ->
            selectedDevice = deviceArray[pos].device
            timer()
        }
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Agregar permisos según la versión del SDK
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        requestMultiplePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
    // Registro del callback para múltiples permisos
    private val requestMultiplePermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissions ->
        permissions.entries.forEach { entry ->
            val permission = entry.key
            val isGranted = entry.value
            if (isGranted) {
                // El permiso ha sido concedido
                println("$permission concedido.")
            } else {
                // El permiso ha sido denegado
                println("$permission denegado.")
            }
        }
    }

    private fun timer() {
        executor.scheduleWithFixedDelay({
            selectedDevice?.let { device ->
                // Buscar el índice del dispositivo seleccionado en la lista
                val index = deviceArray.indexOfFirst { it.device.deviceName == device.deviceName }
                if (index != -1) {
                    // Llamar a updateDescription con el índice del dispositivo seleccionado
                    if (!devicesWithReceivedMessages.contains(device.deviceName)) {
                        // Si es la primera vez que recibes un mensaje del dispositivo, procésalo
                        runOnUiThread {
                            // Actualiza la descripción o realiza las operaciones necesarias con el dispositivo
                            updateDescription(index)
                        }
                        // Marca el dispositivo como uno del que ya has recibido un mensaje
                        devicesWithReceivedMessages.add(device.deviceName)
                    }
                }
            }
        }, 0, interval, TimeUnit.MILLISECONDS)
    }

    private fun updateDescription(selectedDevice: Int) {

        val selectedDeviceInfo = deviceArray[selectedDevice]
        msgSend.text = selectedDeviceInfo.getAllMessage()

    }

    private fun exqListener() {
        btnDiscover.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT > Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED)
            ) {

                Toast.makeText(this, "Faltan pemisos 1", Toast.LENGTH_SHORT).show()
                requestPermissions()

                return@setOnClickListener
            }
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    connectionStatus.setText("Discovery funciona")
                }

                override fun onFailure(reason: Int) {
                    connectionStatus.setText("Discovery no funciona")
                }
            })
        } //Se va

        op1Button.setOnClickListener {
            msg = op1Button.text.toString()

            Toast.makeText(
                applicationContext,
                "Mensaje enviado",
                Toast.LENGTH_LONG
            ).show()

            messages.add(msg.trim() + " - Hora de envío: " + getFormattedDateTime())
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

            messages.add(msg.trim() + " - Hora de envío: " + getFormattedDateTime())
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

            messages.add(msg.trim() + " - Hora de envío: " + getFormattedDateTime())
            saveMessage(msg)
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messages.size - 1)
            clearLocalServices {
                startRegistration()
            }
        }

    }

    fun initialWork() {
        connectionStatus = findViewById(R.id.connectionStatus)
        recyclerView = findViewById(R.id.messageRecyclerView)
        btnDiscover = findViewById(R.id.discover)
        listView = findViewById(R.id.peerListView)
        op1Button = findViewById(R.id.op1Button)
        op2Button = findViewById(R.id.op2Button)
        op3Button = findViewById(R.id.op3Button)
        msgSend = findViewById(R.id.msgSend)

        wifiManager = this.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        manager = getSystemService(WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        WifiFrameUtils.getUUIDWiFiFrame(this)
    }

    private fun startRegistration() {

        info = WifiFrameUtils.buildMyWiFiFrame(this)

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
            if (record.isEmpty() || srcDevice.deviceName == "" || WifiFrameUtils.deviceIdMultiHop == WifiFrameUtils.idDevice.toString() || wifiFrame.sendMessage.isEmpty()) return@DnsSdTxtRecordListener

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
        manager?.clearLocalServices(channel,
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

            if (!messageExist)
                deviceSame.message.add(wifiFrame)

        } else {
            var message =
                MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)

            deviceArray.add(message)
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            deviceArray

        )
        listView.adapter = adapter
        multihop(WifiFrameUtils.deviceIdMultiHop)
    }

    private fun multihop(deviceId: String) {

        var deviceMulti = deviceArray.first {
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
                Toast.makeText(
                    this,
                    "Se ha agregado el mensaje retransmitido por " + record.deviceName,
                    Toast.LENGTH_SHORT
                ).show()
                deviceSame.message.add(wifiFrame)
            }

        } else {
            var device =
                MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)

            deviceArray.add(device)
            Toast.makeText(
                this,
                "Se ha agregado el dispositivo retransmitido por " + record.deviceName,
                Toast.LENGTH_SHORT
            ).show()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            deviceArray

        )
        listView.adapter = adapter

    }

}



