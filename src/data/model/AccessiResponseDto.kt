package com.myhub.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessiResponseDto(
    val value: List<AccessoPc> = emptyList(),
    @SerialName("Count") val count: Int = 0
)