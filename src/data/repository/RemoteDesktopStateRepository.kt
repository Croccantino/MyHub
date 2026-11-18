package com.myhub.app.data.repository

import com.myhub.app.data.model.Alert
import com.myhub.app.data.model.AlertSeverity
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.LoginEntry
import com.myhub.app.data.model.Metrics
import com.myhub.app.data.model.TailscaleStatus
import com.myhub.app.data.remote.DesktopStateApi
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteDesktopStateRepository(
    private val api: DesktopStateApi = com.myhub.app.data.remote.OkHttpDesktopStateApi()
) : DesktopStateRepository {

    override fun metrics(server: DesktopServer?): Flow<Metrics> = flow {
        val s = server ?: run { emit(Metrics()); return@flow }
        while (true) {
            try {
                val m = api.metriche(s)
                val g = api.gpu(s)
                emit(
                    Metrics(
                        cpuCores = m.cpuCores,
                        gpu = g.gpu,
                        ram = m.ram,
                        temp = m.temp
                    )
                )
            } catch (e: Exception) {
                emit(Metrics())
            }
            delay(1500)
        }
    }

    override suspend fun alerts(server: DesktopServer?): List<Alert> {
        val s = server ?: return emptyList()
        return try {
            api.alert(s).alert.map { dto ->
                Alert(
                    severity = when (dto.severity.uppercase()) {
                        "HIGH" -> AlertSeverity.HIGH
                        "MEDIUM" -> AlertSeverity.MEDIUM
                        else -> AlertSeverity.LOW
                    },
                    summary = dto.summary,
                    title = dto.title,
                    description = dto.description
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun loginHistory(server: DesktopServer?): List<LoginEntry> {
        val s = server ?: return emptyList()
        return try {
            val response = api.accessi(s, 10)
            response.map { accesso ->
                val workstation = accesso.dettagli?.get("workstation")
                val device = when {
                    workstation == null || workstation is kotlinx.serialization.json.JsonNull -> accesso.utente?.ifBlank { null } ?: accesso.metodo?.ifBlank { null } ?: "sconosciuto"
                    else -> {
                        val content = (workstation as? kotlinx.serialization.json.JsonPrimitive)?.content
                        if (content.isNullOrBlank()) accesso.utente?.ifBlank { null } ?: accesso.metodo?.ifBlank { null } ?: "sconosciuto"
                        else content
                    }
                }
                val dateTime = formattaDataOra(accesso.ts)
                LoginEntry(
                    device = device,
                    ip = accesso.ip ?: "sconosciuto",
                    dateTime = dateTime
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun tailscaleStatus(server: DesktopServer?): TailscaleStatus {
        val s = server ?: return TailscaleStatus(connected = false, deviceName = "sconosciuto")
        return try {
            val t = api.tailscale(s)
            TailscaleStatus(connected = t.connected, deviceName = t.deviceName)
        } catch (e: Exception) {
            TailscaleStatus(connected = false, deviceName = "sconosciuto")
        }
    }

    override suspend fun toggleTailscale(server: DesktopServer?) {
        // TODO: endpoint non disponibile
    }

    override suspend fun refresh() { /* no-op */ }

    private fun formattaDataOra(testo: String?): String {
        if (testo.isNullOrBlank()) return "non disponibile"
        return try {
            val instant = try { Instant.parse(testo) }
                catch (_: Exception) { OffsetDateTime.parse(testo).toInstant() }
            val zona = ZoneId.systemDefault()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ITALIAN)
            instant.atZone(zona).format(formatter)
        } catch (_: Exception) {
            testo
        }
    }
}