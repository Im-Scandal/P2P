package com.example.p2papp

class WifiFrame {
    var sendMessage: String = "Mensaje"
    var dateSend: String = "Fecha"
    var dateReceived: String = "FechaRecibida"
    var nameMultiHop: String = ""

    fun getMessage() =
        "\n - Mensaje: ${sendMessage} ${if (nameMultiHop.isNotEmpty()) " (Retrasmitido por "+nameMultiHop+")" else ""}" +
                "\n Hora de envio: ${dateSend}" +
                "\n Hora de llegada: ${dateReceived}"

}