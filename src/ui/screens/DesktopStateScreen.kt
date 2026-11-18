package com.myhub.app.ui.screens

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myhub.app.data.model.AlertSeverity
import com.myhub.app.ui.components.BottomBar
import com.myhub.app.ui.components.desktopstate.LineChart
import com.myhub.app.ui.theme.ConnectedGreen
import com.myhub.app.ui.theme.DisconnectedRed

private val CpuColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopStateScreen(
    onOpenDrawer: () -> Unit = {},
    onHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val repositoryDesktop = remember { com.myhub.app.data.repository.DesktopRepository.getInstance(context) }
    val repository = remember { com.myhub.app.data.repository.RemoteDesktopStateRepository() }
    val viewModel: DesktopStateViewModel = viewModel(
        factory = remember(repository, repositoryDesktop) {
            com.myhub.app.ui.screens.DesktopStateViewModelFactory(repository, repositoryDesktop.activeServerFlow)
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val scale = Settings.Global.getFloat(
            context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
        )
        viewModel.onReduceMotionChanged(scale == 0f)
    }

    BackHandler(enabled = state.expandedChart != null) { viewModel.toggleChart(state.expandedChart ?: "") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Desktop State") },
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = onOpenDrawer) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.myhub.app.R.drawable.logo),
                        contentDescription = "Menu",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.senzaServer) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Nessun server configurato. Vai su Preferenze per aggiungerne uno.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(24.dp))
                }
            } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedContent(
                    targetState = state.expandedChart,
                    transitionSpec = {
                        if (state.reduceMotion) {
                            (fadeIn(tween(0)) togetherWith fadeOut(tween(0)))
                        } else {
                            (fadeIn(tween(250)) togetherWith fadeOut(tween(250)))
                        }
                    },
                    label = "chartDetail"
                ) { expanded ->
                    if (expanded != null) {
                        ChartDetailView(state = state, viewModel = viewModel, expanded = expanded)
                    } else {
                        NormalGridView(state = state, viewModel = viewModel)
                    }
                }
                AlertsSection(state = state, viewModel = viewModel)
                LoginHistoryButton(viewModel = viewModel, onClick = { viewModel.setShowLoginHistory(true) })
            }
            }
        }

        BottomBar(
            connected = state.tailscale.connected,
            onHelp = { viewModel.setShowHelp(true) },
            onReload = { viewModel.refresh() },
            onHome = onHome,
            onToggleConnection = { viewModel.setShowTailscale(true) }
        )
    }

    if (state.showHelp) {
        HelpSheet(onDismiss = { viewModel.setShowHelp(false) })
    }
    if (state.showTailscale) {
        TailscaleSheet(
            status = state.tailscale,
            onToggle = { viewModel.toggleTailscale() },
            onDismiss = { viewModel.setShowTailscale(false) }
        )
    }
    if (state.showLoginHistory) {
        LoginHistorySheet(
            entries = state.loginHistory,
            onDismiss = { viewModel.setShowLoginHistory(false) }
        )
    }
}

@Composable
private fun NormalGridView(state: DesktopStateUiState, viewModel: DesktopStateViewModel) {
    val history = state.history
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartCard(title = "CPU", value = "${state.metrics?.cpuAvg?.toInt() ?: 0}%",
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = { viewModel.toggleChart("CPU") }) {
                val series = (0..3).map { i -> history.map { it.cpuCores.getOrElse(i) { 0f } } }
                LineChart(series = series, colors = CpuColors, modifier = Modifier.fillMaxSize())
            }
            ChartCard(title = "GPU", value = "${state.metrics?.gpu?.toInt() ?: 0}%",
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = { viewModel.toggleChart("GPU") }) {
                LineChart(series = listOf(history.map { it.gpu }), colors = listOf(Color(0xFF8E24AA)), modifier = Modifier.fillMaxSize())
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChartCard(title = "RAM", value = "${state.metrics?.ram?.toInt() ?: 0}%",
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = { viewModel.toggleChart("RAM") }) {
                LineChart(series = listOf(history.map { it.ram }), colors = listOf(Color(0xFF00897B)), modifier = Modifier.fillMaxSize())
            }
            ChartCard(title = "Temperatura", value = "${state.metrics?.temp?.toInt() ?: 0}°C",
                modifier = Modifier.weight(1f).height(140.dp),
                onClick = { viewModel.toggleChart("TEMP") }) {
                LineChart(series = listOf(history.map { it.temp }), colors = listOf(Color(0xFFF4511E)), modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxSize()) { content() }
        }
    }
}

@Composable
private fun ChartDetailView(state: DesktopStateUiState, viewModel: DesktopStateViewModel, expanded: String) {
    val history = state.history
    val largeTitle: String
    val largeValue: String
    val largeColors: List<Color>
    val largeSeries: List<List<Float>>
    val showLegend = expanded == "CPU"
    when (expanded) {
        "CPU" -> {
            largeTitle = "CPU"
            largeValue = "${state.metrics?.cpuAvg?.toInt() ?: 0}%"
            largeColors = CpuColors
            largeSeries = (0..3).map { i -> history.map { it.cpuCores.getOrElse(i) { 0f } } }
        }
        "GPU" -> {
            largeTitle = "GPU"
            largeValue = "${state.metrics?.gpu?.toInt() ?: 0}%"
            largeColors = listOf(Color(0xFF8E24AA))
            largeSeries = listOf(history.map { it.gpu })
        }
        "RAM" -> {
            largeTitle = "RAM"
            largeValue = "${state.metrics?.ram?.toInt() ?: 0}%"
            largeColors = listOf(Color(0xFF00897B))
            largeSeries = listOf(history.map { it.ram })
        }
        "TEMP" -> {
            largeTitle = "Temperatura"
            largeValue = "${state.metrics?.temp?.toInt() ?: 0}°C"
            largeColors = listOf(Color(0xFFF4511E))
            largeSeries = listOf(history.map { it.temp })
        }
        else -> return
    }

    val smallCharts = listOf("CPU", "GPU", "RAM", "TEMP").filter { it != expanded }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clickable { viewModel.toggleChart(expanded) },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(largeTitle, style = MaterialTheme.typography.titleMedium)
                    Text(largeValue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                if (showLegend) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (0..3).forEach { i ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Canvas(Modifier.size(10.dp)) { drawCircle(color = CpuColors[i]) }
                                Spacer(Modifier.width(2.dp))
                                Text("C${i + 1}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Box(Modifier.fillMaxSize()) {
                    LineChart(series = largeSeries, colors = largeColors, modifier = Modifier.fillMaxSize(), showGrid = true)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            smallCharts.forEach { chart ->
                val (t, v, pair) = smallChartInfo(chart, state, history)
                val (c, s) = pair
                ChartCard(title = t, value = v, modifier = Modifier.weight(1f).height(100.dp),
                    onClick = { viewModel.toggleChart(chart) }) {
                    LineChart(series = s, colors = c, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

private fun smallChartInfo(chart: String, state: DesktopStateUiState, history: List<com.myhub.app.data.model.Metrics>): Triple<String, String, Pair<List<Color>, List<List<Float>>>> {
    return when (chart) {
        "CPU" -> Triple("CPU", "${state.metrics?.cpuAvg?.toInt() ?: 0}%", Pair(CpuColors, (0..3).map { i -> history.map { it.cpuCores.getOrElse(i) { 0f } } }))
        "GPU" -> Triple("GPU", "${state.metrics?.gpu?.toInt() ?: 0}%", Pair(listOf(Color(0xFF8E24AA)), listOf(history.map { it.gpu })))
        "RAM" -> Triple("RAM", "${state.metrics?.ram?.toInt() ?: 0}%", Pair(listOf(Color(0xFF00897B)), listOf(history.map { it.ram })))
        "TEMP" -> Triple("Temp", "${state.metrics?.temp?.toInt() ?: 0}°C", Pair(listOf(Color(0xFFF4511E)), listOf(history.map { it.temp })))
        else -> Triple("", "", Pair(emptyList(), emptyList()))
    }
}

@Composable
private fun AlertsSection(state: DesktopStateUiState, viewModel: DesktopStateViewModel) {
    Text("Criticità sul server", style = MaterialTheme.typography.titleMedium)
    state.alerts.forEachIndexed { index, alert ->
        val barColor = when (alert.severity) {
            AlertSeverity.HIGH -> DisconnectedRed
            AlertSeverity.MEDIUM -> Color(0xFFF9A825)
            AlertSeverity.LOW -> ConnectedGreen
        }
        val expanded = state.expandedAlert == index
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.expandAlert(index) },
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(barColor)
                )
                Column(Modifier.padding(12.dp).weight(1f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(alert.summary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Icon(
                            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    if (expanded) {
                        Spacer(Modifier.height(4.dp))
                        Text(alert.title, style = MaterialTheme.typography.titleSmall)
                        Text(alert.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginHistoryButton(viewModel: DesktopStateViewModel, onClick: () -> Unit) {
    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    color = Color.Gray,
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f))
                )
            }
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.History, contentDescription = "Cronologia dei login")
            Spacer(Modifier.width(8.dp))
            Text("Cronologia dei login", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HelpSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(20.dp)) {
            Text("Guida", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Questa schermata mostra lo stato del desktop: CPU, GPU, RAM e temperatura in tempo reale.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginHistorySheet(entries: List<com.myhub.app.data.model.LoginEntry>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(20.dp)) {
            Text("Cronologia dei login", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            if (entries.isEmpty()) {
                Text("Nessun accesso registrato.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                entries.forEach { e ->
                    Text("${e.device}", style = MaterialTheme.typography.titleSmall)
                    Text("IP: ${e.ip}  •  ${e.dateTime}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TailscaleSheet(
    status: com.myhub.app.data.model.TailscaleStatus,
    onToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(20.dp)) {
            Text("Tailscale", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Stato: ${if (status.connected) "Connesso" else "Disconnesso"}")
            Text("Dispositivo: ${status.deviceName}")
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.Button(onClick = onToggle) {
                Text(if (status.connected) "Disconnetti" else "Connetti")
            }
        }
    }
}
