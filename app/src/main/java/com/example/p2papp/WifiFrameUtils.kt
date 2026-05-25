package com.example.p2papp

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class WifiFrameUtils {

    companion object {

        var deviceMultihop = ""
        var deviceIdMultiHop = ""
        var idDevice = ""
        fun wifiFrameToHashMap(wifiFrame: WifiFrame?): HashMap<String, String> {
            val message = HashMap<String, String>()
            deviceMultihop = ""
            deviceIdMultiHop = ""

            if (wifiFrame == null) {
                return message
            }

            message["n"] = wifiFrame.nameUser
            message["o"] = wifiFrame.sendMessage
            message["d"] = wifiFrame.dateSend
            message["h"] = idDevice
            message["t"] = wifiFrame.type
            wifiFrame.latitude?.let { message["la"] = it.toString() }
            wifiFrame.longitude?.let { message["lo"] = it.toString() }
            message["x"] = wifiFrame.pruebaI
            message["y"] = wifiFrame.pruebaII
            message["z"] = wifiFrame.pruebaIII
            message["a"] = wifiFrame.pruebaIV
            message["b"] = wifiFrame.pruebaV

            return message
        }

        fun wifiFrameToHashMapMultihop(
            deviceName: WifiP2pDevice,
            messageMulti: String = "",
            id: String,
            dateSend: String,
            nameUser: String,
            latitude: Double? = null,
            longitude: Double? = null,
            type: String,
            pruebaI: String,
            pruebaII: String,
            pruebaIII: String,
            pruebaIV: String,
            pruebaV: String,
        ): HashMap<String, String> {
            val message = HashMap<String, String>()

            message["n"] = nameUser
            message["d"] = dateSend
            message["g"] = deviceName.deviceName
            message["o"] = messageMulti
            message["h"] = id
            latitude?.let { message["la"] = it.toString() }
            longitude?.let { message["lo"] = it.toString() }
            message["t"] = type
            message["x"] = pruebaI
            message["y"] = pruebaII
            message["z"] = pruebaIII
            message["a"] = pruebaIV
            message["b"] = pruebaV

            return message
        }

        fun hashMapToWiFiFrame(message: MutableMap<String, String>): WifiFrame {
            return WifiFrame().apply {
                nameUser = message["n"] ?: "Desconocido"
                sendMessage = message["o"]?: ""
                dateSend = message["d"]?: "0L"
                dateReceived =  getFormattedDateTime()
                deviceMultihop = message["g"]?: ""
                deviceIdMultiHop = message["h"]?: ""
                latitude = message["la"]?.toDoubleOrNull()
                longitude = message["lo"]?.toDoubleOrNull()
                type = message["t"]?: "CHAT"
                pruebaI = message["x"]?: "Prueba 1"
                pruebaII = message["y"]?: "Prueba 2"
                pruebaIII = message["z"]?: "Prueba 3"
                pruebaIV = message["a"]?: "Prueba 4"
                pruebaV = message["b"]?: "Prueba 5"
            }
        }

        fun buildMyWiFiFrame(context: Context, userName: String, typePackage: String): WifiFrame {
            val sharedPreferences = context.getSharedPreferences(
                Constants.PREFERENCES_KEY,
                AppCompatActivity.MODE_PRIVATE
            )

            return WifiFrame().apply {
                nameUser = userName
                sendMessage = sharedPreferences.getString(Constants.MESSAGE, "mensaje").toString()
                dateSend = getFormattedDateTime()

                val latStr = sharedPreferences.getString("LATITUDE", null)
                val lonStr = sharedPreferences.getString("LONGITUDE", null)
                latitude = latStr?.toDoubleOrNull()
                longitude = lonStr?.toDoubleOrNull()

                type = typePackage

                pruebaI = sharedPreferences.getString("pruebaI", null).toString()
                pruebaII = sharedPreferences.getString("pruebaII", null).toString()
                pruebaIII = sharedPreferences.getString("pruebaIII", null).toString()
                pruebaIV = sharedPreferences.getString("pruebaIV", null).toString()
                pruebaV = sharedPreferences.getString("pruebaV", null).toString()
            }
        }

        fun getUUIDWiFiFrame(context: Context) {
            val sharedPreferences = context.getSharedPreferences(
                Constants.PREFERENCES_KEY,
                AppCompatActivity.MODE_PRIVATE
            )
            idDevice = sharedPreferences.getString(Constants.UUID, UUID.randomUUID().toString())?: UUID.randomUUID().toString()
            val editor = sharedPreferences.edit()
            editor.putString(Constants.UUID, idDevice)
            editor.apply()

        }



    }


}