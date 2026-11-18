package com.myhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MetricheDto(
    @SerialName("cpu_cores") val cpuCores: List<Float> = emptyList(),
    val gpu: Float = 0f,
    val ram: Float = 0f,
    val temp: Float = 0f
)

@Serializable
data class GpuDto(
    @SerialName("utilizzo_percent") val utilizzoPercent: Int = 0,
    val nome: String = "",
    @SerialName("temperatura_c") val temperaturaC: Int = 0,
    @SerialName("memoria_usata_mb") val memoriaUsataMb: Int = 0,
    @SerialName("memoria_totale_mb") val memoriaTotaleMb: Int = 0
) {
    val gpu: Float get() = utilizzoPercent.toFloat()
}

@Serializable
data class AlertDto(
    val severity: String = "LOW",
    val summary: String = "",
    val title: String = "",
    val description: String = ""
)

@Serializable
data class AlertResponseDto(
    val alert: List<AlertDto> = emptyList()
)

@Serializable
data class TailscaleStatusDto(
    val connesso: Boolean = false,
    @SerialName("device_name") val deviceName: String = "",
    @SerialName("ip_tailnet") val ipTailnet: String = ""
) {
    val connected: Boolean get() = connesso
}
