package com.myhub.app.data.remote

import com.myhub.app.data.model.AccessoPc
import com.myhub.app.data.model.CampioneIo
import com.myhub.app.data.model.DiscoInfo
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.StatoAgente
import com.myhub.app.data.security.KeystoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Errori che l'agente può restituire o che si possono verificare in rete. */
sealed class ErroreAgente(message: String) : Exception(message) {
    data object TokenNonValido : ErroreAgente("Token non valido")
    data object NonRaggiungibile : ErroreAgente("Server non raggiungibile")
    data class RispostaNonValida(override val message: String) : ErroreAgente(message)
}

/** Chiamate all'agente locale del PC. */
interface AgenteApi {
    suspend fun stato(server: DesktopServer): StatoAgente
    suspend fun io(server: DesktopServer, minuti: Int = 30): List<CampioneIo>
    suspend fun accessi(server: DesktopServer, limite: Int = 50, offset: Int = 0): List<AccessoPc>
    suspend fun disco(server: DesktopServer): List<DiscoInfo>
}

/**
 * Client OkHttp verso l'agente.
 *
 * L'agente usa HTTP in chiaro: il traffico passa dentro Tailscale e il
 * `network_security_config` consente il cleartext solo per i domini `ts.net`.
 */
class OkHttpAgenteApi : AgenteApi {

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

    override suspend fun stato(server: DesktopServer): StatoAgente =
        get(server, "/stato")

    override suspend fun io(server: DesktopServer, minuti: Int): List<CampioneIo> =
        get(server, "/io", listOf("minuti" to minuti.toString()))

    override suspend fun accessi(
        server: DesktopServer,
        limite: Int,
        offset: Int
    ): List<AccessoPc> = get(
        server,
        "/accessi",
        listOf(
            "limit" to limite.toString(),
            "offset" to offset.toString()
        )
    )

    override suspend fun disco(server: DesktopServer): List<DiscoInfo> =
        get(server, "/disco")

    private suspend inline fun <reified T> get(
        server: DesktopServer,
        path: String,
        query: List<Pair<String, String>> = emptyList()
    ): T = withContext(Dispatchers.IO) {
        val baseUrl = "http://${server.host}:${server.port}".toHttpUrlOrNull()
            ?: throw ErroreAgente.RispostaNonValida("Indirizzo del server non valido")

        val url = baseUrl.newBuilder()
            .addPathSegments(path.trimStart('/'))
            .apply { query.forEach { (nome, valore) -> addQueryParameter(nome, valore) } }
            .build()

        val token = try {
            KeystoreHelper.decrypt(server.encryptedToken, server.tokenIv)
        } catch (errore: Exception) {
            throw ErroreAgente.RispostaNonValida("Token non decifrabile")
        }

        val richiesta = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            client.newCall(richiesta).execute().use { risposta ->
                when {
                    risposta.code == 401 -> throw ErroreAgente.TokenNonValido
                    risposta.code !in 200..299 ->
                        throw ErroreAgente.RispostaNonValida("HTTP ${risposta.code}")
                    else -> {
                        val corpo = risposta.body?.string()
                            ?: throw ErroreAgente.RispostaNonValida("Risposta vuota")
                        try {
                            json.decodeFromString<T>(corpo)
                        } catch (errore: Exception) {
                            throw ErroreAgente.RispostaNonValida(
                                "Risposta non valida: ${errore.message ?: "formato sconosciuto"}"
                            )
                        }
                    }
                }
            }
        } catch (errore: ErroreAgente) {
            throw errore
        } catch (errore: IOException) {
            throw ErroreAgente.NonRaggiungibile
        } catch (errore: Exception) {
            throw ErroreAgente.RispostaNonValida(errore.message ?: "Errore sconosciuto")
        }
    }
}
