package com.myhub.app.data.repository

import com.myhub.app.data.model.AccessoPc
import com.myhub.app.data.model.CampioneIo
import com.myhub.app.data.model.DiscoInfo
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.EventoAccesso
import com.myhub.app.data.model.StatoAgente
import com.myhub.app.data.remote.AgenteApi
import com.myhub.app.data.remote.OkHttpAgenteApi
import java.time.Instant
import kotlin.math.sin

/** Sorgente dei dati mostrati nella schermata "Access". */
interface DatiRepository {
    suspend fun stato(server: DesktopServer): StatoAgente
    suspend fun io(server: DesktopServer, minuti: Int = 30): List<CampioneIo>
    suspend fun accessi(server: DesktopServer, limite: Int = 50, offset: Int = 0): List<AccessoPc>
    suspend fun disco(server: DesktopServer): List<DiscoInfo>
}

/** Implementazione reale: parla con l'agente tramite [AgenteApi]. */
class AgenteDatiRepository(
    private val api: AgenteApi = OkHttpAgenteApi()
) : DatiRepository {

    override suspend fun stato(server: DesktopServer): StatoAgente = api.stato(server)

    override suspend fun io(server: DesktopServer, minuti: Int): List<CampioneIo> =
        api.io(server, minuti)

    override suspend fun accessi(server: DesktopServer, limite: Int, offset: Int): List<AccessoPc> =
        api.accessi(server, limite, offset)

    override suspend fun disco(server: DesktopServer): List<DiscoInfo> =
        api.disco(server)
}

/**
 * Repository finto, utile per anteprime e test senza agente.
 * Genera una serie I/O sinusoidale e un elenco di accessi credibili.
 */
class FakeDatiRepository : DatiRepository {

    override suspend fun stato(server: DesktopServer): StatoAgente =
        StatoAgente(hostname = "PC-DEMO", uptimeSecondi = 7_265)

    override suspend fun io(server: DesktopServer, minuti: Int): List<CampioneIo> {
        val adesso = Instant.now()
        val campioni = minuti * 12
        return (0 until campioni).map { indice ->
            val secondiFa = (campioni - indice) * 5L
            val fase = indice / 12.0
            CampioneIo(
                ts = adesso.minusSeconds(secondiFa).toString(),
                letturaMbS = 12.0 + 8.0 * sin(fase) + (indice % 7) * 0.4,
                scritturaMbS = 5.0 + 4.0 * sin(fase + 1.2) + (indice % 5) * 0.3
            )
        }
    }

    override suspend fun accessi(server: DesktopServer, limite: Int, offset: Int): List<AccessoPc> {
        val adesso = Instant.now()
        return (0 until limite).map { indice ->
            val assoluto = offset + indice
            val riuscito = assoluto % 3 != 0
            AccessoPc(
                id = assoluto.toLong(),
                ts = adesso.minusSeconds(assoluto * 1_800L).toString(),
                utente = "demo",
                metodo = "rdp",
                ip = if (riuscito) "100.64.1.10" else "93.44.12.5",
                esito = if (riuscito) "ok" else "fallito",
                posizione = if (riuscito) "Rete locale / Tailscale" else "Milano, Italia",
                tracciamento = listOf(
                    EventoAccesso(
                        ts = adesso.minusSeconds(assoluto * 1_800L).toString(),
                        evento = if (riuscito) "Accesso riuscito (RDP)" else "Accesso fallito (RDP)"
                    )
                ),
                azioni = if (riuscito) {
                    listOf(
                        EventoAccesso(
                            ts = adesso.minusSeconds(assoluto * 1_800L - 120).toString(),
                            evento = "Processo avviato: explorer.exe"
                        )
                    )
                } else {
                    emptyList()
                }
            )
        }
    }

    override suspend fun disco(server: DesktopServer): List<DiscoInfo> = listOf(
        DiscoInfo(
            dispositivo = "C:\\",
            punto = "C:\\",
            filesystem = "NTFS",
            totaleGb = 446.1,
            usatoGb = 262.5,
            liberoGb = 183.6,
            percentuale = 58.9
        ),
        DiscoInfo(
            dispositivo = "D:\\",
            punto = "D:\\",
            filesystem = "NTFS",
            totaleGb = 931.5,
            usatoGb = 519.3,
            liberoGb = 412.2,
            percentuale = 55.8
        )
    )
}
