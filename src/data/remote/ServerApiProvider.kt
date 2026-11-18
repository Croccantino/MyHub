package com.myhub.app.data.remote

import com.myhub.app.data.ServerApi

object ServerApiProvider {
    const val BASE_URL = "https://desktop-fh8gf7h.tail4db350.ts.net:8443/"
    fun create(): ServerApi = RetrofitServerApi(BASE_URL)
}