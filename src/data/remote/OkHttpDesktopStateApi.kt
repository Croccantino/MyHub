package com.myhub.app.data.remote

import com.myhub.app.data.model.AccessoPc
import com.myhub.app.data.model.AlertDto
import com.myhub.app.data.model.AlertResponseDto
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.GpuDto
import com.myhub.app.data.model.MetricheDto
import com.myhub.app.data.model.TailscaleStatusDto
import com.myhub.app.data.security.KeystoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class OkHttpDesktopStateApi : DesktopStateApi {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    override suspend fun metriche(server: DesktopServer): MetricheDto =
        get(server, "/metriche")

    override suspend fun gpu(server: DesktopServer): GpuDto =
        get(server, "/gpu")

    override suspend fun alert(server: DesktopServer): AlertResponseDto =
        get(server, "/alert")

    override suspend fun tailscale(server: DesktopServer): TailscaleStatusDto =
        get(server, "/tailscale")

    override suspend fun accessi(server: DesktopServer, limit: Int): List<AccessoPc> =
        get(server, "/accessi", listOf("limit" to limit.toString()))

    private suspend inline fun <reified T> get(
        server: DesktopServer,
        path: String,
        query: List<Pair<String, String>> = emptyList()
    ): T = withContext(Dispatchers.IO) {
        val baseUrl = "http://${server.host}:${server.port}".toHttpUrlOrNull()
            ?: throw Exception("Indirizzo del server non valido")

        val url = baseUrl.newBuilder()
            .addPathSegments(path.trimStart('/'))
            .apply { query.forEach { (nome, valore) -> addQueryParameter(nome, valore) } }
            .build()

        val token = try {
            KeystoreHelper.decrypt(server.encryptedToken, server.tokenIv)
        } catch (e: Exception) {
            throw Exception("Token non decifrabile")
        }

        val richiesta = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            client.newCall(richiesta).execute().use { risposta ->
                when {
                    risposta.code == 401 -> throw Exception("Token non valido")
                    risposta.code !in 200..299 ->
                        throw Exception("HTTP ${risposta.code}")
                    else -> {
                        val corpo = risposta.body?.string()
                            ?: throw Exception("Risposta vuota")
                        try {
                            json.decodeFromString<T>(corpo)
                        } catch (e: Exception) {
                            throw Exception("Risposta non valida: ${e.message ?: "formato sconosciuto"}")
                        }
                    }
                }
            }
        } catch (e: IOException) {
            throw Exception("Server non raggiungibile")
        } catch (e: Exception) {
            throw e
        }
    }
}

interface DesktopStateApi {
    suspend fun metriche(server: DesktopServer): MetricheDto
    suspend fun gpu(server: DesktopServer): GpuDto
    suspend fun alert(server: DesktopServer): AlertResponseDto
    suspend fun tailscale(server: DesktopServer): TailscaleStatusDto
    suspend fun accessi(server: DesktopServer, limit: Int): List<AccessoPc>
}