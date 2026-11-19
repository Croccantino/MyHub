package com.myhub.app.data

import com.myhub.app.model.Accesso
import com.myhub.app.model.Server

object FakeData {
    val server = Server(
        nome = "DESKTOP-ABC123",
        indirizzo = "100.111.246.64",
        online = true
    )

    val accessi = listOf(
        Accesso("192.168.1.20", "18/11/2026", "10:32:11", "SSH", "ok", "Accesso SSH"),
        Accesso("192.168.1.45", "18/11/2026", "09:58:40", "RDP", "fallito", "Login RDP"),
        Accesso("10.0.0.7", "17/11/2026", "23:14:02", "DB", "ok", "Query database")
    )
}
