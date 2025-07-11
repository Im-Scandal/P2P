package com.example.p2papp

import android.net.wifi.p2p.WifiP2pDevice

data class MessageModel(var device: WifiP2pDevice,
    var message: MutableList<WifiFrame>,
    var id: String){

    override fun toString(): String {
        return device.deviceName
    }
}
