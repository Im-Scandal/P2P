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
            wifiFrame.latitude?.let { message["la"] = it.toString() }
            wifiFrame.longitude?.let { message["lo"] = it.toString() }

            return message
        }

        fun wifiFrameToHashMapMultihop(
            deviceName: WifiP2pDevice,
            messageMulti: String = "",
            id: String,
            dateSend: String,
            nameUser: String,
            latitude: Double? = null,
            longitude: Double? = null
        ): HashMap<String, String> {
            val message = HashMap<String, String>()

            message["n"] = nameUser
            message["d"] = dateSend
            message["g"] = deviceName.deviceName
            message["o"] = messageMulti
            message["h"] = id
            latitude?.let { message["la"] = it.toString() }
            longitude?.let { message["lo"] = it.toString() }

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
            }
        }

        fun buildMyWiFiFrame(context: Context, userName: String): WifiFrame {
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