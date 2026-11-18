package com.myhub.app.data.model

/** Snapshot delle metriche del PC. */
data class Metrics(
    val cpuCores: List<Float> = List(4) { 0f },
    val gpu: Float = 0f,
    val ram: Float = 0f,
    val temp: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    val cpuAvg: Float get() = if (cpuCores.isEmpty()) 0f else cpuCores.average().toFloat()
}

enum class AlertSeverity { HIGH, MEDIUM, LOW }

data class Alert(
    val severity: AlertSeverity,
    val summary: String,
    val title: String,
    val description: String
)

data class LoginEntry(
    val device: String,
    val ip: String,
    val dateTime: String
)

data class TailscaleStatus(
    val connected: Boolean,
    val deviceName: String
)