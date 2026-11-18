package com.myhub.app.data.remote

import com.myhub.app.data.Item
import retrofit2.http.GET

interface PostgRestApi {
    @GET("items")
    suspend fun getItems(): List<Item>
}