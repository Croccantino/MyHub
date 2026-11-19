package com.myhub.app.data

import com.myhub.app.model.Accesso
import com.myhub.app.model.Server

data class HomeCardInfo(val titolo: String, val sottotitolo: String)

object FakeData {
    val homeCard = listOf(
        HomeCardInfo("Desktop state", "CPU 23% · RAM 41%"),
        HomeCardInfo("Disco", "C: 120 GB liberi"),
        HomeCardInfo("Remote control", "Desktop remoto"),
        HomeCardInfo("Dispositivi collegati", "3 online"),
        HomeCardInfo("Terminal", "SSH")
    )

    val server = Server(
        nome = "DESKTOP-ABC123",
        indirizzo = "100.111.246.64",
        online = true
    )

    val desktop = listOf(
        Server("DESKTOP-ABC123", "100.111.246.64", true),
        Server("DESKTOP-XYZ789", "100.111.246.65", false),
        Server("DESKTOP-DEF456", "100.111.246.66", true)
    )

    val accessi = listOf(
        Accesso("192.168.1.20", "18/11/2026", "10:32:11", "SSH", "ok", "Accesso SSH"),
        Accesso("192.168.1.45", "18/11/2026", "09:58:40", "RDP", "fallito", "Login RDP"),
        Accesso("10.0.0.7", "17/11/2026", "23:14:02", "DB", "ok", "Query database")
    )

    val lettura = List(20) { i -> 20f + (i * 7 % 30) + ((i * 13) % 11).toFloat() }
    val scrittura = List(20) { i -> 10f + (i * 5 % 20) + ((i * 9) % 7).toFloat() }
}
