package com.myhub.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myhub.app.data.model.Alert
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.LoginEntry
import com.myhub.app.data.model.Metrics
import com.myhub.app.data.model.TailscaleStatus
import com.myhub.app.data.repository.DesktopStateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DesktopStateUiState(
    val metrics: Metrics? = null,
    val history: List<Metrics> = emptyList(),
    val alerts: List<Alert> = emptyList(),
    val expandedChart: String? = null,
    val expandedAlert: Int? = null,
    val isRefreshing: Boolean = false,
    val showHelp: Boolean = false,
    val showTailscale: Boolean = false,
    val showLoginHistory: Boolean = false,
    val tailscale: TailscaleStatus = TailscaleStatus(false, "PC-DEMO"),
    val loginHistory: List<LoginEntry> = emptyList(),
    val reduceMotion: Boolean = false,
    val senzaServer: Boolean = false
)

class DesktopStateViewModel(
    private val repository: DesktopStateRepository,
    serverFlow: Flow<DesktopServer?>
) : ViewModel() {

    private val _uiState = MutableStateFlow(DesktopStateUiState())
    val uiState: StateFlow<DesktopStateUiState> = _uiState.asStateFlow()

    private var server: DesktopServer? = null
    private var lavoroServer: Job? = null
    private var lavoroMetrics: Job? = null

    init {
        lavoroServer = viewModelScope.launch {
            serverFlow.collect { nuovo ->
                val precedente = server
                server = nuovo
                when {
                    nuovo == null -> {
                        lavoroMetrics?.cancel()
                        _uiState.value = DesktopStateUiState(senzaServer = true, reduceMotion = _uiState.value.reduceMotion)
                    }
                    nuovo != precedente -> {
                        _uiState.value = DesktopStateUiState(reduceMotion = _uiState.value.reduceMotion)
                        caricaDati()
                        avviaMetrics()
                    }
                }
            }
        }
    }

    private fun caricaDati() {
        val s = server ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(alerts = repository.alerts(s)) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loginHistory = repository.loginHistory(s)) }
        }
        viewModelScope.launch {
            _uiState.update { it.copy(tailscale = repository.tailscaleStatus(s)) }
        }
    }

    private fun avviaMetrics() {
        val s = server ?: return
        lavoroMetrics?.cancel()
        lavoroMetrics = viewModelScope.launch {
            repository.metrics(s).collect { m ->
                _uiState.update { state ->
                    val newHistory = (state.history + m).takeLast(30)
                    state.copy(metrics = m, history = newHistory)
                }
            }
        }
    }

    fun toggleChart(chart: String) {
        _uiState.update { it.copy(expandedChart = if (it.expandedChart == chart) null else chart) }
    }

    fun expandAlert(index: Int?) {
        _uiState.update { s ->
            s.copy(expandedAlert = if (s.expandedAlert == index) null else index)
        }
    }

    fun setShowHelp(show: Boolean) { _uiState.update { it.copy(showHelp = show) } }
    fun setShowTailscale(show: Boolean) { _uiState.update { it.copy(showTailscale = show) } }
    fun setShowLoginHistory(show: Boolean) {
        _uiState.update { it.copy(showLoginHistory = show) }
        if (show) {
            val s = server
            viewModelScope.launch {
                _uiState.update { it.copy(loginHistory = repository.loginHistory(s)) }
            }
        }
    }

    fun toggleTailscale() {
        val s = server ?: return
        viewModelScope.launch {
            repository.toggleTailscale(s)
            _uiState.update { it.copy(tailscale = repository.tailscaleStatus(s)) }
        }
    }

    fun refresh() {
        val s = server ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            _uiState.update { it.copy(alerts = repository.alerts(s), isRefreshing = false) }
        }
    }

    fun onReduceMotionChanged(value: Boolean) {
        _uiState.update { it.copy(reduceMotion = value) }
    }
}

class DesktopStateViewModelFactory(
    private val repository: DesktopStateRepository,
    private val serverFlow: Flow<DesktopServer?>
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DesktopStateViewModel(repository, serverFlow) as T
    }
}