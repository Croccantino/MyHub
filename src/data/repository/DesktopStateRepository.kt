package com.myhub.app.data.repository

import com.myhub.app.data.model.Alert
import com.myhub.app.data.model.AlertSeverity
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.LoginEntry
import com.myhub.app.data.model.Metrics
import com.myhub.app.data.model.TailscaleStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

interface DesktopStateRepository {
    fun metrics(server: DesktopServer?): Flow<Metrics>
    suspend fun alerts(server: DesktopServer?): List<Alert>
    suspend fun loginHistory(server: DesktopServer?): List<LoginEntry>
    suspend fun tailscaleStatus(server: DesktopServer?): TailscaleStatus
    suspend fun toggleTailscale(server: DesktopServer?)
    suspend fun refresh()
}

class FakeDesktopStateRepository : DesktopStateRepository {
    private var tailscaleConnected = true

    override fun metrics(server: DesktopServer?): Flow<Metrics> = flow {
        var seed = 0
        while (true) {
            val cores = List(4) { (30 + Random.nextFloat() * 50).coerceIn(0f, 100f) }
            emit(
                Metrics(
                    cpuCores = cores,
                    gpu = (20 + Random.nextFloat() * 60).coerceIn(0f, 100f),
                    ram = (40 + Random.nextFloat() * 40).coerceIn(0f, 100f),
                    temp = (35 + Random.nextFloat() * 30).coerceIn(0f, 100f)
                )
            )
            seed++
            delay(1500)
        }
    }

    override suspend fun alerts(server: DesktopServer?): List<Alert> = listOf(
        Alert(AlertSeverity.HIGH, "1 errore da risolvere", "Servizio X non risponde", "Il servizio X non risponde da 2 ore. Riavviare il servizio."),
        Alert(AlertSeverity.MEDIUM, "2 avvisi", "CPU elevata", "La CPU ha superato il 80% per 5 minuti consecutivi."),
        Alert(AlertSeverity.LOW, "Nessun problema", "Tutto ok", "Nessuna criticità rilevata nelle ultime 24 ore.")
    )

    override suspend fun loginHistory(server: DesktopServer?): List<LoginEntry> = listOf(
        LoginEntry("PC-1", "192.168.1.10", "09/11/2026 10:30"),
        LoginEntry("Laptop-2", "192.168.1.20", "09/11/2026 09:15"),
        LoginEntry("Telefono", "100.64.1.5", "08/11/2026 22:00")
    )

    override suspend fun tailscaleStatus(server: DesktopServer?): TailscaleStatus =
        TailscaleStatus(connected = tailscaleConnected, deviceName = "PC-DEMO")

    override suspend fun toggleTailscale(server: DesktopServer?) {
        tailscaleConnected = !tailscaleConnected
    }

    override suspend fun refresh() { /* no-op for fake */ }
}