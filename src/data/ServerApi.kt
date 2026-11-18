package com.myhub.app.data

interface ServerApi {
    suspend fun fetchDati(): List<Item>
    suspend fun fetchDesktopState(): String
    suspend fun fetchDevices(): List<String>
    suspend fun fetchTerminal(): String
}

class FakeServerApi : ServerApi {
    override suspend fun fetchDati(): List<Item> = listOf(Item(1, "uno"), Item(2, "due"))
    override suspend fun fetchDesktopState(): String = "Online"
    override suspend fun fetchDevices(): List<String> = listOf("PC-1", "Laptop-2")
    override suspend fun fetchTerminal(): String = "Terminal ready"
}