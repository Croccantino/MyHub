package com.myhub.app.data.model

import kotlinx.serialization.Serializable

/** Services that a desktop server can have enabled. */
enum class DesktopService { REMOTE_DESKTOP, SSH, DATABASE }

/** Health-check result sealed class. */
sealed class HealthResult {
    data class Success(val ok: Boolean, val db: Boolean) : HealthResult()
    data object TokenInvalid : HealthResult()
    data object ServerUnreachable : HealthResult()
    data object DbUnreachable : HealthResult()
}

/** Reachability state for status dots. */
enum class ReachabilityState { UNKNOWN, REACHABLE, UNREACHABLE }

/**
 * A saved desktop / server entry.
 * The API token is stored encrypted (Base64 cipher-text + Base64 IV).
 */
@Serializable
data class DesktopServer(
    val id: String = "",
    val name: String = "",
    val host: String = "",
    val port: Int = 8443,
    val encryptedToken: String = "",
    val tokenIv: String = "",
    val services: Set<DesktopService> = emptySet(),
    val order: Int = 0
)
