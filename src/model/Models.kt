package com.myhub.app.model

data class Server(
    val nome: String,
    val indirizzo: String,
    val online: Boolean
)

data class Accesso(
    val ip: String,
    val data: String,
    val ora: String,
    val canale: String,
    val esito: String,
    val azione: String
)
