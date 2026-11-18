package com.myhub.app.data.remote

import com.myhub.app.data.config.ServerConfig
import okhttp3.Interceptor
import okhttp3.Response

/** Interceptor che aggiunge il token Bearer alle richieste (non collegato per ora). */
class TokenAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer ${ServerConfig.TOKEN}")
            .build()
        return chain.proceed(request)
    }
}
