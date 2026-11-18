package com.myhub.app.ui.components.dati

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN)
private val formatoOra = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ITALIAN)
private val formatoDataOra = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ITALIAN)
private val formatoOraMinuti = DateTimeFormatter.ofPattern("HH:mm", Locale.ITALIAN)

/** Converte una stringa ISO8601 in [Instant]; null se assente o non valida. */
fun istanteDa(testo: String?): Instant? {
    if (testo.isNullOrBlank()) return null
    return runCatching { Instant.parse(testo) }
        .recoverCatching { OffsetDateTime.parse(testo).toInstant() }
        .getOrNull()
}

private fun formatta(
    testo: String?,
    formato: DateTimeFormatter,
    fallback: String,
    zona: ZoneId = ZoneId.systemDefault()
): String = istanteDa(testo)?.atZone(zona)?.format(formato) ?: fallback

/** Data nel fuso del telefono, `GG/MM/AAAA`. */
fun formattaData(testo: String?, fallback: String): String =
    formatta(testo, formatoData, fallback)

/** Ora nel fuso del telefono, `hh:mm:ss`. */
fun formattaOra(testo: String?, fallback: String): String =
    formatta(testo, formatoOra, fallback)

/** Data e ora nel fuso del telefono. */
fun formattaDataOra(testo: String?, fallback: String): String =
    formatta(testo, formatoDataOra, fallback)

/** Solo ore e minuti, per le etichette dei grafici. */
fun formattaOraMinuti(testo: String?, fallback: String): String =
    formatta(testo, formatoOraMinuti, fallback)

/** Uptime in `hh:mm:ss`, senza limite di ore. */
fun formattaUptime(secondi: Long): String {
    val totale = secondi.coerceAtLeast(0)
    val ore = totale / 3600
    val minuti = (totale % 3600) / 60
    val secondiRestanti = totale % 60
    return String.format(Locale.ITALIAN, "%02d:%02d:%02d", ore, minuti, secondiRestanti)
}
