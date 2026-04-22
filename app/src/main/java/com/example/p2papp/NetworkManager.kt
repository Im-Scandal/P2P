package com.example.p2papp

import android.Manifest
import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.example.p2papp.Constants.TAG_WIFI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NetworkManager {

    companion object {
        private var manager: WifiP2pManager? = null
        private var channel: WifiP2pManager.Channel? = null

        var deviceArray: MutableList<MessageModel> = mutableListOf()

        private val processedMessages = mutableSetOf<String>()

        fun setup(context: Context) {
            if (manager != null) return
            val appContext = context.applicationContext
            manager = appContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            channel = manager?.initialize(appContext, appContext.mainLooper, null)
            configureDnsSdListeners()
        }

        private fun configureDnsSdListeners() {
            val txtListener = WifiP2pManager.DnsSdTxtRecordListener { _, record, srcDevice ->
                val wifiFrame = WifiFrameUtils.hashMapToWiFiFrame(record as MutableMap<String, String>)

                // Validaciones de seguridad
                if (record.isEmpty() || srcDevice.deviceName == "" ||
                    WifiFrameUtils.deviceIdMultiHop == WifiFrameUtils.idDevice ||
                    wifiFrame.sendMessage.isEmpty()) return@DnsSdTxtRecordListener

                // 1. Actualizar nuestra lista global deviceArray
                updateGlobalDeviceList(srcDevice, wifiFrame)

                // 2. Control de duplicados para retransmisión
                val msgId = "${wifiFrame.nameUser}_${wifiFrame.dateSend}"
                if (processedMessages.contains(msgId)) return@DnsSdTxtRecordListener
                processedMessages.add(msgId)

                // 3. Notificar a la UI (Radar/Chat)
                CoroutineScope(Dispatchers.IO).launch {
                    RadarEvent.emitRadarPing(wifiFrame)
                }

                // 4. Retransmisión Automática si es CHAT
                if (wifiFrame.type == "CHAT") {
                    // Aquí podrías usar el ID para retransmitir
                    multihop(WifiFrameUtils.deviceIdMultiHop)
                }
            }
            manager?.setDnsSdResponseListeners(channel, null, txtListener)
        }


        fun multihop(deviceId: String) {
            try {
                val deviceMulti = deviceArray.firstOrNull { it.id == deviceId }

                deviceMulti?.let {

                    val record = WifiFrameUtils.wifiFrameToHashMapMultihop(
                        deviceMulti.device,
                        deviceMulti.message.last().sendMessage,
                        deviceMulti.id,
                        deviceMulti.message.last().dateSend,
                        deviceMulti.message.last().nameUser // Aquí pasas el nombre del emisor original
                    )

                    val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                        "_networkChat", "_chatApp._tcp", record
                    )

                    manager?.clearLocalServices(channel, object : WifiP2pManager.ActionListener {
                        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
                        override fun onSuccess() {
                            manager?.addLocalService(channel, serviceInfo, null)
                        }
                        override fun onFailure(p0: Int) {}
                    })
                }
            } catch (e: Exception) {
                Log.e(TAG_WIFI, "Error en retransmisión por ID: ${e.message}")
            }
        }

        private fun updateGlobalDeviceList(device: WifiP2pDevice, frame: WifiFrame) {
            // Lógica para añadir o actualizar el dispositivo en deviceArray
            // (Similar a lo que tenías en addDeviceList del MainChat)
        }

        fun startDiscovery() {
            val request = WifiP2pDnsSdServiceRequest.newInstance("_networkChat", "_chatApp._tcp")
            manager?.addServiceRequest(channel, request, object : WifiP2pManager.ActionListener {
                @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES])
                override fun onSuccess() { manager?.discoverServices(channel, null) }
                override fun onFailure(p0: Int) {}
            })
        }
    }
}