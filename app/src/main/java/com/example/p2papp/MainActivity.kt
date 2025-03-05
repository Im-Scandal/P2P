package com.example.p2papp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var btnSend: Button
    private lateinit var listView: ListView
    private lateinit var readMsgBox: TextView
    private lateinit var writeMsg: EditText
    private lateinit var recyclerView: RecyclerView

    private lateinit var manager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: BroadcastReceiver
    private lateinit var intentFilter: IntentFilter

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
        btnSend.setOnClickListener {
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    writeMsg.hint = "Discovery funciona"
                }

                override fun onFailure(reason: Int) {
                    writeMsg.hint = "Discovery no funciona"
                }
            })
        }

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, i, _ ->
                val device = deviceArray[i]
                val config = WifiP2pConfig().apply {
                    deviceAddress = device.deviceAddress
                }
                manager.connect(channel, config, object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        writeMsg.hint = "Connected: ${device.deviceAddress}"
                    }

                    override fun onFailure(reason: Int) {
                        writeMsg.hint = "No connected"
                    }
                })
            }
    }

    private fun initialWork() {
        btnSend = findViewById(R.id.sendButton)
        listView = findViewById(R.id.peerListView)
        readMsgBox = findViewById(R.id.messageTextView)
        writeMsg = findViewById(R.id.messageEditText)
        recyclerView = findViewById(R.id.messageRecyclerView)

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

    private val peerListListener = WifiP2pManager.PeerListListener { wifiP2pDeviceList ->
        if (wifiP2pDeviceList.deviceList.isNotEmpty()) {
            // Limpiar y rellenar las listas con los dispositivos detectados
            peers.clear()
            peers.addAll(wifiP2pDeviceList.deviceList)

            deviceNameArray = Array(peers.size) { index ->
                peers[index].deviceName
            }
        } else {
            // Si no hay dispositivos, mostrar mensaje en la lista
            deviceNameArray = arrayOf("No device found")
        }

        // Crear un adaptador para actualizar el ListView
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            deviceNameArray
        )
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
