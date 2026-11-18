package com.myhub.app.ui.navigation

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Dati : Route("dati")
    data object Access : Route("access")
    data object Preferenze : Route("preferenze")
    data object Impostazioni : Route("impostazioni")
    data object Desktop : Route("desktop")
    data object Remote : Route("remote")
    data object Devices : Route("devices")
    data object Terminal : Route("terminal")
    data object Help : Route("help")
}
