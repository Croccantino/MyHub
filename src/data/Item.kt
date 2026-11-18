package com.myhub.app.data

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val id: Int? = null,
    val name: String
)