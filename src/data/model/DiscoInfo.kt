package com.myhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Informazioni su una partizione disco del PC. */
@Serializable
data class DiscoInfo(
    val dispositivo: String = "",
    val punto: String = "",
    val filesystem: String = "",
    @SerialName("totale_gb") val totaleGb: Double = 0.0,
    @SerialName("usato_gb") val usatoGb: Double = 0.0,
    @SerialName("libero_gb") val liberoGb: Double = 0.0,
    val percentuale: Double = 0.0
)