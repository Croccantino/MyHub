package com.myhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Risposta di `GET /stato`. */
@Serializable
data class StatoAgente(
    val hostname: String? = null,
    val sistema: String? = null,
    @SerialName("uptime_secondi") val uptimeSecondi: Long = 0,
    @SerialName("avvio_secondi") val avvioSecondi: Long = 0,
    @SerialName("cpu_percent") val cpuPercent: Double = 0.0,
    @SerialName("ram_percent") val ramPercent: Double = 0.0
)

/** Singolo campionamento di `GET /io`. */
@Serializable
data class CampioneIo(
    val ts: String = "",
    @SerialName("lettura_mb_s") val letturaMbS: Double = 0.0,
    @SerialName("scrittura_mb_s") val scritturaMbS: Double = 0.0
)

/** Evento della cronologia di un accesso. */
@Serializable
data class EventoAccesso(
    val ts: String? = null,
    val evento: String? = null
)

/** Riga di `GET /accessi`. I campi opzionali possono mancare. */
@Serializable
data class AccessoPc(
    val id: Long? = null,
    val ts: String? = null,
    val utente: String? = null,
    val metodo: String? = null,
    val ip: String? = null,
    val esito: String? = null,
    val dettagli: JsonObject? = null,
    val posizione: String? = null,
    val tracciamento: List<EventoAccesso> = emptyList(),
    val azioni: List<EventoAccesso> = emptyList()
) {
    /** Vero se il server ha registrato l'accesso come riuscito. */
    val riuscito: Boolean
        get() = esito.equals("ok", ignoreCase = true)
}
