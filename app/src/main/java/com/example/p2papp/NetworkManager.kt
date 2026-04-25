package com.example.p2papp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import com.example.p2papp.Constants.TAG
import com.example.p2papp.Constants.TAG_WIFI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class NetworkManager {

    companion object {
        private var manager: WifiP2pManager? = null
        private var channel: WifiP2pManager.Channel? = null

        var deviceArray: MutableList<MessageModel> = mutableListOf()

        fun setup(context: Context) {
            if (manager != null) return
            val appContext = context.applicationContext
            manager = appContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            channel = manager?.initialize(appContext, appContext.mainLooper, null)
//            configureDnsSdListeners()
        }

        fun addServiceRequest(context: Context) {
            val serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(
                "_networkChat",
                "_chatApp._tcp"
            )

            manager?.addServiceRequest(
                channel,
                serviceRequest,
                object : WifiP2pManager.ActionListener {
                    override fun onSuccess() {
                        discoverListener(context)
                    }

                    override fun onFailure(code: Int) {
                        Log.e(TAG_WIFI, "Add service request has failed. $code")
                    }
                }
            )
        }

        private val executor = Executors.newSingleThreadScheduledExecutor()
        private var task: Runnable? = null
        private var interval: Long = 10000
        private val devicesWithReceivedMessages: MutableSet<String> = mutableSetOf()


        fun startDiscover(context: Context) {
            // Crea un nuevo Runnable que se ejecutará después de cada intervalo
            task = Runnable {
                discoverServices(context)
                clearReceivedDevicesAfterDelay()
            }
            // Programa el primer ciclo del temporizador con un retraso inicial de 0 y un intervalo especificado
            executor.scheduleWithFixedDelay(task!!, 0, interval, TimeUnit.MILLISECONDS)
        }

        fun discoverServices(context: Context) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                (Build.VERSION.SDK_INT > Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                return
            }
            else { manager?.discoverServices(
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

        private fun discoverListener(context: Context) {

            val txtListener = WifiP2pManager.DnsSdTxtRecordListener { fullDomain, record, srcDevice ->
                Log.d("PAQUETE_EN_AIRE", "DnsSdTxtRecord available -$record")
                val wifiFrame = WifiFrameUtils.hashMapToWiFiFrame(record)
                if (record.isEmpty() || srcDevice.deviceName == "" || WifiFrameUtils.deviceIdMultiHop == WifiFrameUtils.idDevice){
                    return@DnsSdTxtRecordListener
                } else {
                    Log.i("PRIMER_FILTRO", "DnsSdTxtRecord available -$record")}

                if (wifiFrame.type == "CHAT" && wifiFrame.sendMessage.isEmpty()){
                    return@DnsSdTxtRecordListener
                } else {
                    Log.i("SEGUNDO_FILTRO", "DnsSdTxtRecord available -$record")}
                if (wifiFrame.type == "RADAR") {
                    val dist = calculateHaversine(wifiFrame.latitude ?: 0.0, wifiFrame.longitude ?: 0.0, context)
                    Log.e("PRUEBA_VIDA_RADAR", "!!! PAQUETE DETECTADO !!! Distancia: $dist metros de ti.")
                } else {
                    Log.i("TERCER_FILTRO", "DnsSdTxtRecord available -$record")
                }

                if (WifiFrameUtils.deviceMultihop.isNotEmpty()) {
                    val deviceP2p = WifiP2pDevice().apply {
                        deviceName = WifiFrameUtils.deviceMultihop
                    }
                    wifiFrame.apply {
                        nameMultiHop = srcDevice.deviceName
                    }
                    addDeviceMultiHop(deviceP2p, wifiFrame, context)
                } else {
                    addDeviceList(srcDevice, wifiFrame, context)
                }

            }

            val servListener =
                WifiP2pManager.DnsSdServiceResponseListener { instanceName, registrationType, resourceType ->
                    Log.d("chat", "BonjourService available! instanceName: $instanceName")
                    Log.d("chat", "BonjourService available! registrationType: $registrationType")
                    Log.d("chat", "BonjourService available! resourceType: $resourceType")
                }


            manager?.setDnsSdResponseListeners(channel, servListener, txtListener)
        }

        fun clearLocalServices(onSuccessCallback: () -> Unit) {
            manager?.clearLocalServices(channel,
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
                    }
                })
        }

        private fun clearReceivedDevicesAfterDelay() {
            // Limpia el conjunto de dispositivos después de cierto tiempo
            executor.schedule({
                devicesWithReceivedMessages.clear()
            }, 500, TimeUnit.MILLISECONDS)
        }

        private fun addDeviceList(record: WifiP2pDevice, wifiFrame: WifiFrame, context: Context) {

            val deviceSame = deviceArray.firstOrNull { it.device.deviceName == record.deviceName }

            if (deviceSame != null) {
                val messageExist = deviceSame.message.any {
                    it.dateSend == wifiFrame.dateSend
                }

                if (!messageExist){
                    deviceSame.message.add(wifiFrame)
                    CoroutineScope(Dispatchers.IO).launch {
                        RadarEvent.emitRadarPing(wifiFrame)
                    }
                }

            } else {
                val message = MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)
                deviceArray.add(message)

                CoroutineScope(Dispatchers.IO).launch {
                    RadarEvent.emitRadarPing(wifiFrame)
                }

            }

            multihop(WifiFrameUtils.deviceIdMultiHop, context)
        }

        private fun multihop(deviceId: String, context: Context) {

            val deviceMulti = deviceArray.first {
                it.id == deviceId
            }

            val record = WifiFrameUtils.wifiFrameToHashMapMultihop(
                deviceMulti.device,
                deviceMulti.message.last().sendMessage,
                deviceMulti.id,
                deviceMulti.message.last().dateSend,
                deviceMulti.message.last().nameUser,
                deviceMulti.message.last().latitude,
                deviceMulti.message.last().longitude,
                deviceMulti.message.last().type
            )

            // Service information.  Pass it an instance name, service type
            val serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_networkChat", "_chatApp._tcp", record)

            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
                        )
            ) {
                Log.e("TEST_ESTABILIDAD", "Faltan permisos")
                return
            }
            manager?.addLocalService(channel, serviceInfo, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG_WIFI, "RE envio exitoso!")
                }

                override fun onFailure(arg0: Int) {
                    // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    Log.e(TAG_WIFI, "Fallo al publicar mensaje: $arg0")
                }
            })
        }

        private fun addDeviceMultiHop(record: WifiP2pDevice, wifiFrame: WifiFrame, context: Context) {

            val deviceSame = deviceArray.firstOrNull { it.device.deviceName == record.deviceName }
            if (deviceSame != null) {
                val messageExist = deviceSame.message.any {
                    it.dateSend == wifiFrame.dateSend
                }

                if (!messageExist) {
                    deviceSame.message.add(wifiFrame)
                    CoroutineScope(Dispatchers.IO).launch {
                        RadarEvent.emitRadarPing(wifiFrame)
                    }
                }

            } else {
                val device = MessageModel(record, mutableListOf(wifiFrame), WifiFrameUtils.deviceIdMultiHop)
                deviceArray.add(device)
                CoroutineScope(Dispatchers.IO).launch {
                    RadarEvent.emitRadarPing(wifiFrame)
                }
            }

        }

        fun calculateHaversine(targetLat: Double, targetLon: Double, context: Context): Int {
            val sharedPref = context.getSharedPreferences(Constants.PREFERENCES_KEY, Context.MODE_PRIVATE)
            val myLat = sharedPref.getString("LATITUDE", "0")?.toDoubleOrNull() ?: 0.0
            val myLon = sharedPref.getString("LONGITUDE", "0")?.toDoubleOrNull() ?: 0.0

            val earthRadius = 6371.0
            val dLat = Math.toRadians(targetLat - myLat)
            val dLon = Math.toRadians(targetLon - myLon)
            val a = Math.sin(dLat / 2).pow(2.0) +
                    Math.cos(Math.toRadians(myLat)) * Math.cos(Math.toRadians(targetLat)) *
                    Math.sin(dLon / 2).pow(2.0)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return (earthRadius * c * 1000).toInt() // Retorna metros
        }

    }
}