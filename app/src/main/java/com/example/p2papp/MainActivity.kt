package com.example.p2papp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity()
{

    //Inicialización de variables de la vista
    private lateinit var btnSend: Button
    private lateinit var btnDiscover: Button
    private lateinit var listView: ListView
    lateinit var readMsgBox: TextView
    private lateinit var connectionStatus: TextView
    private lateinit var writeMsg: EditText
    private lateinit var recyclerView: RecyclerView

    //Variables para WiFi p2p
    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

    //Variables para agregar los dispositivos (peers) al listview
    private val peers = mutableListOf<WifiP2pDevice>()
    private lateinit var deviceNameArray: Array<String>
    private lateinit var deviceArray: Array<WifiP2pDevice>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialWork()
        exqListener()
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
        }

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, i, _ ->
                if (::deviceArray.isInitialized && deviceArray.isNotEmpty()) {
                    val device = deviceArray[i]
                    val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
                    manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                        override fun onSuccess() {
                            readMsgBox.setText("Connected: ${device.deviceAddress}")
                        }

                        override fun onFailure(reason: Int) {
                            readMsgBox.setText("Not connected")
                        }
                    })
                } else {
                    Toast.makeText(this, "No device available", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun initialWork() {
        connectionStatus = findViewById(R.id.connectionStatus)
        recyclerView = findViewById(R.id.messageRecyclerView)
        btnDiscover = findViewById(R.id.discover)
        listView = findViewById(R.id.peerListView)
        writeMsg = findViewById(R.id.writeMsg)
        readMsgBox = findViewById(R.id.readMsg)
        btnSend = findViewById(R.id.sendButton)

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
        receiver = WiFiDirectBroadcastReceiver(manager, channel, this)

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    val peerListListener = WifiP2pManager.PeerListListener { wifiP2pDeviceList ->
        if (wifiP2pDeviceList.deviceList.isNotEmpty()) {
            // Limpiar y rellenar las listas con los dispositivos detectados
            peers.clear()
            peers.addAll(wifiP2pDeviceList.deviceList)
            deviceNameArray = Array(peers.size) { index -> peers[index].deviceName }
            deviceArray = peers.toTypedArray() // Aquí se inicializa correctamente
        } else {
            // Si no hay dispositivos, mostrar mensaje en la lista
            deviceNameArray = arrayOf("No device found")
            deviceArray = emptyArray() // Asegurarse de que también se inicialice vacío si no hay dispositivos
        }

        // Crear un adaptador para actualizar el ListView
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNameArray)
        listView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }
}
