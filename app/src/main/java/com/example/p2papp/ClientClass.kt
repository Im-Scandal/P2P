//package com.example.p2papp
//
//import android.os.Handler
//import android.os.Looper
//import java.io.IOException
//import java.io.InputStream
//import java.io.OutputStream
//import java.net.InetSocketAddress
//import java.net.Socket
//import java.util.concurrent.Executors
//
//class ClientClass (
//    private val activity: MainActivity?
//){
//    private lateinit var inputStream: InputStream
//    private lateinit var outputStream: OutputStream
//    private val hostAdd: String = activity?.hostAddress.hostAddress
//    private val socket = Socket()
//
//    override fun run() {
//        try {
//            socket.connect(InetSocketAddress(hostAdd, 8888), 500)
//            inputStream = socket.getInputStream()
//            outputStream = socket.getOutputStream()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//        val executor = Executors.newSingleThreadExecutor()
//        val handler = Handler(Looper.getMainLooper())
//
//        executor.execute {
//            val buffer = ByteArray(1024)
//            var bytes: Int
//
//            while (socket.isConnected) {
//                try {
//                    bytes = inputStream.read(buffer)
//                    if (bytes > 0) {
//                        val finalBytes = bytes
//                        handler.post {
//                            val tempMSG = String(buffer, 0, finalBytes)
//                            activity?.readMsgBox?.setText(tempMSG)?
//                        }
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }
//}