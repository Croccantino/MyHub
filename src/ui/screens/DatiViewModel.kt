package com.myhub.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myhub.app.data.model.CampioneIo
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.DiscoInfo
import com.myhub.app.data.remote.ErroreAgente
import com.myhub.app.data.repository.DatiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Stato generale della schermata. */
enum class FaseDati { CARICAMENTO, PRONTO, ERRORE, SENZA_SERVER }

/** Errore mostrabile all'utente, tradotto in stringa dalla UI. */
enum class ErroreDati { TOKEN, RETE, GENERICO }

data class DatiUiState(
    val fase: FaseDati = FaseDati.CARICAMENTO,
    val errore: ErroreDati? = null,
    val uptimeSecondi: Long = 0,
    val campioniIo: List<CampioneIo> = emptyList(),
    val ioNonDisponibile: Boolean = false,
    val dischi: List<DiscoInfo> = emptyList(),
    val discoNonDisponibile: Boolean = false,
    val refreshInCorso: Boolean = false
)

/**
 * ViewModel della schermata "Access".
 *
 * - carica stato, I/O e primi 50 accessi dal server attivo;
 * - fa avanzare l'uptime ogni secondo e lo risincronizza ogni 60 secondi;
 * - aggiorna i grafici ogni 5 secondi, ma solo con la schermata visibile.
 */
class DatiViewModel(
    private val repository: DatiRepository,
    serverFlow: Flow<DesktopServer?>
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatiUiState())
    val uiState: StateFlow<DatiUiState> = _uiState.asStateFlow()

    private var server: DesktopServer? = null
    private var visibile = false

    private var lavoroCaricamento: Job? = null
    private var lavoroServer: Job? = null
    private var lavoroIo: Job? = null
    private var lavoroStato: Job? = null

    init {
        lavoroServer = viewModelScope.launch {
            serverFlow.collect { nuovo ->
                val precedente = server
                server = nuovo
                when {
                    nuovo == null -> {
                        lavoroCaricamento?.cancel()
                        lavoroIo?.cancel()
                        lavoroStato?.cancel()
                        _uiState.value = DatiUiState(fase = FaseDati.SENZA_SERVER)
                    }
                    nuovo != precedente -> {
                        _uiState.value = DatiUiState(fase = FaseDati.CARICAMENTO)
                        caricaTutto(mostraCaricamento = true)
                        avviaPolling()
                    }
                }
            }
        }
    }

    /** Da chiamare quando la schermata entra in stato STARTED. */
    fun onVisibile() {
        if (visibile) return
        visibile = true
        avviaPolling()
    }

    /** Da chiamare quando la schermata non è più visibile. */
    fun onNonVisibile() {
        visibile = false
        lavoroIo?.cancel()
        lavoroIo = null
        lavoroStato?.cancel()
        lavoroStato = null
    }

    /** Ricarica tutto senza mostrare lo spinner a tutta pagina. */
    fun refresh() {
        if (_uiState.value.refreshInCorso) return
        caricaTutto(mostraCaricamento = false)
    }

    /** Riprova dopo un errore bloccante. */
    fun riprova() {
        caricaTutto(mostraCaricamento = true)
    }

    private fun caricaTutto(mostraCaricamento: Boolean) {
        val server = server ?: return
        lavoroCaricamento?.cancel()
        lavoroCaricamento = viewModelScope.launch {
            if (mostraCaricamento) {
                _uiState.update { it.copy(fase = FaseDati.CARICAMENTO, errore = null) }
            } else {
                _uiState.update { it.copy(refreshInCorso = true, errore = null) }
            }

            val stato = runCatching { repository.stato(server) }
            val dischi = runCatching { repository.disco(server) }
            val io = runCatching { repository.io(server, 30) }

            val errore = when {
                stato.isFailure -> stato.exceptionOrNull()
                dischi.isFailure -> dischi.exceptionOrNull()
                else -> null
            }

            if (errore != null) {
                val haGiaDati = _uiState.value.dischi.isNotEmpty()
                _uiState.update {
                    it.copy(
                        fase = if (mostraCaricamento || !haGiaDati) {
                            FaseDati.ERRORE
                        } else {
                            FaseDati.PRONTO
                        },
                        errore = mappaErrore(errore),
                        refreshInCorso = false
                    )
                }
                return@launch
            }

            val nuoviDischi = dischi.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    fase = FaseDati.PRONTO,
                    errore = null,
                    uptimeSecondi = stato.getOrNull()?.uptimeSecondi ?: it.uptimeSecondi,
                    dischi = nuoviDischi,
                    campioniIo = io.getOrDefault(emptyList()),
                    ioNonDisponibile = io.isFailure,
                    discoNonDisponibile = dischi.isFailure,
                    refreshInCorso = false
                )
            }
        }
    }

    private fun avviaPolling() {
        lavoroIo?.cancel()
        lavoroStato?.cancel()
        if (!visibile || server == null) return

        lavoroIo = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                val attivo = server ?: continue
                runCatching { repository.io(attivo, 30) }
                    .onSuccess { campioni ->
                        val attuali = _uiState.value.campioniIo
                        if (campioni != attuali) {
                            _uiState.update {
                                it.copy(campioniIo = campioni, ioNonDisponibile = false)
                            }
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(ioNonDisponibile = true) }
                    }
            }
        }

        lavoroStato = viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                val attivo = server ?: continue
                runCatching { repository.stato(attivo) }
                    .onSuccess { stato ->
                        _uiState.update { it.copy(uptimeSecondi = stato.uptimeSecondi) }
                    }
            }
        }
    }

    private fun mappaErrore(errore: Throwable?): ErroreDati = when (errore) {
        is ErroreAgente.TokenNonValido -> ErroreDati.TOKEN
        is ErroreAgente.NonRaggiungibile -> ErroreDati.RETE
        else -> ErroreDati.GENERICO
    }

    override fun onCleared() {
        lavoroServer?.cancel()
        lavoroCaricamento?.cancel()
        lavoroIo?.cancel()
        lavoroStato?.cancel()
        super.onCleared()
    }
}

class DatiViewModelFactory(
    private val repository: DatiRepository,
    private val serverFlow: Flow<DesktopServer?>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatiViewModel::class.java)) {
            return DatiViewModel(repository, serverFlow) as T
        }
        throw IllegalArgumentException("ViewModel sconosciuto: ${modelClass.name}")
    }
}
