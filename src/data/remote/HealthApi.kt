package com.myhub.app.data.remote

import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.HealthResult
import com.myhub.app.data.security.KeystoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object HealthApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun check(desktop: DesktopServer): HealthResult = withContext(Dispatchers.IO) {
        val token = KeystoreHelper.decrypt(desktop.encryptedToken, desktop.tokenIv)
        val url = "http://${desktop.host}:${desktop.port}/health"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                when (response.code) {
                    401 -> return@withContext HealthResult.TokenInvalid
                    in 200..299 -> {
                        val body = response.body?.string()
                            ?: return@withContext HealthResult.ServerUnreachable
                        return@withContext try {
                            val json = JSONObject(body)
                            val ok = json.optBoolean("ok", false)
                            val db = json.optBoolean("db", false)
                            if (ok && db) HealthResult.Success(ok = true, db = true)
                            else if (ok) HealthResult.Success(ok = true, db = false)
                            else HealthResult.ServerUnreachable
                        } catch (_: Exception) {
                            HealthResult.ServerUnreachable
                        }
                    }
                    else -> return@withContext HealthResult.ServerUnreachable
                }
            }
        } catch (_: IOException) {
            HealthResult.ServerUnreachable
        } catch (_: Exception) {
            HealthResult.ServerUnreachable
        }
    }
}
