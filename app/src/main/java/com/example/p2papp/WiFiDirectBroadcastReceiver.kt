package com.example.p2papp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import java.net.NetworkInterface

class WiFiDirectBroadcastReceiver(
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?,
    private val activity: MainActivity?
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Implementar comportamiento para el cambio de estado del Wi-Fi Direct
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // Implementar comportamiento para el cambio en los pares disponibles
                manager?.requestPeers(channel, activity?.peerListListener)
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Implementar comportamiento para el cambio en la conectividad
                val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                if (networkInfo?.isConnected == true) {
                    manager?.requestConnectionInfo(channel, activity?.connectionInfoListener)
                } else {
                    activity?.connectionStatus?.text = "Not Connected"
                }
            }
        }
    }
}
