package com.myhub.app.data.remote

import com.myhub.app.data.Item
import com.myhub.app.data.ServerApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class RetrofitServerApi(baseUrl: String) : ServerApi {
    private val json = Json { ignoreUnknownKeys = true }
    private val api: PostgRestApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PostgRestApi::class.java)

    override suspend fun fetchDati(): List<Item> = api.getItems()
    override suspend fun fetchDesktopState(): String = "Online"
    override suspend fun fetchDevices(): List<String> = emptyList()
    override suspend fun fetchTerminal(): String = ""
}