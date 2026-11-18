package com.myhub.app.data.model

data class StatoPc(val cpu: Int, val ram: Int, val disco: Int, val online: Boolean)
data class FileItem(val nome: String, val percorso: String, val dimensione: Long)
data class Accesso(val utente: String, val ip: String, val quando: String)
data class Dispositivo(val nome: String, val connesso: Boolean)
data class TerminaleOutput(val linee: List<String>)