package com.myhub.app.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myhub.app.R
import com.myhub.app.data.model.DesktopServer
import com.myhub.app.data.model.DesktopService
import com.myhub.app.data.model.HealthResult
import com.myhub.app.data.model.ReachabilityState
import com.myhub.app.data.remote.HealthApi
import com.myhub.app.data.repository.DesktopRepository
import com.myhub.app.data.security.KeystoreHelper
import com.myhub.app.ui.theme.ConnectedGreen
import com.myhub.app.ui.theme.DisconnectedRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun PreferenzeScreen() {
    val context = LocalContext.current
    val repository = remember { DesktopRepository.getInstance(context) }
    val desktops by repository.desktopsFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Health states keyed by desktop id
    var healthStates by remember { mutableStateOf<Map<String, ReachabilityState>>(emptyMap()) }

    // Dialog states
    var editingDesktop by remember { mutableStateOf<DesktopServer?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var deletingDesktop by remember { mutableStateOf<DesktopServer?>(null) }

    // Refresh all health states
    suspend fun refreshAll() {
        desktops.forEach { desktop ->
            healthStates = healthStates + (desktop.id to ReachabilityState.UNKNOWN)
            val result = HealthApi.check(desktop)
            val state = when (result) {
                is HealthResult.Success -> ReachabilityState.REACHABLE
                else -> ReachabilityState.UNREACHABLE
            }
            healthStates = healthStates + (desktop.id to state)
        }
    }

    // Poll every 30s only while the screen is visible; stops in background.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, desktops) {
        if (desktops.isEmpty()) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                refreshAll()
                delay(30_000)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (desktops.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.pref_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(
                    items = desktops.sortedBy { it.order },
                    key = { _, d -> d.id }
                ) { index, desktop ->
                    DesktopRowItem(
                        desktop = desktop,
                        reachability = healthStates[desktop.id] ?: ReachabilityState.UNKNOWN,
                        onMoveUp = if (index > 0) {
                            {
                                scope.launch {
                                    repository.moveDesktop(index, index - 1)
                                }
                            }
                        } else null,
                        onMoveDown = if (index < desktops.size - 1) {
                            {
                                scope.launch {
                                    repository.moveDesktop(index, index + 1)
                                }
                            }
                        } else null,
                        onClick = { editingDesktop = desktop }
                    )
                }
            }
        }

        // Add desktop button
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.pref_add_desktop))
        }
    }

    // Add dialog
    if (showAddDialog) {
        DesktopEditDialog(
            desktop = null,
            onDismiss = { showAddDialog = false },
            onSave = { newDesktop ->
                scope.launch {
                    repository.addDesktop(newDesktop)
                    showAddDialog = false
                }
            },
            onDelete = null
        )
    }

    // Edit dialog
    editingDesktop?.let { desktop ->
        DesktopEditDialog(
            desktop = desktop,
            onDismiss = { editingDesktop = null },
            onSave = { updated ->
                scope.launch {
                    repository.updateDesktop(updated)
                    editingDesktop = null
                }
            },
            onDelete = { deletingDesktop = desktop; editingDesktop = null }
        )
    }

    // Delete confirmation
    deletingDesktop?.let { desktop ->
        AlertDialog(
            onDismissRequest = { deletingDesktop = null },
            title = { Text(stringResource(R.string.pref_elimina)) },
            text = { Text(stringResource(R.string.pref_elimina_conferma)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        repository.removeDesktop(desktop.id)
                        deletingDesktop = null
                    }
                }) {
                    Text(stringResource(R.string.pref_conferma))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingDesktop = null }) {
                    Text(stringResource(R.string.annulla))
                }
            }
        )
    }
}

// ──────────────────── Row item ────────────────────

@Composable
private fun DesktopRowItem(
    desktop: DesktopServer,
    reachability: ReachabilityState,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Move buttons (up/down arrows replacing drag handle)
            Column {
                if (onMoveUp != null) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.UnfoldMore, contentDescription = "Move up",
                            modifier = Modifier.size(20.dp))
                    }
                }
                if (onMoveDown != null && onMoveUp == null) {
                    // Spacer so layout stays consistent
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }

            Icon(
                Icons.Filled.UnfoldMore,
                contentDescription = "Drag handle",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Desktop name
            Text(
                text = desktop.name.ifBlank { desktop.host },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            // Status dots
            Spacer(modifier = Modifier.width(8.dp))
            StatusDot(
                active = reachability == ReachabilityState.REACHABLE,
                activeColor = ConnectedGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            StatusDot(
                active = reachability == ReachabilityState.UNREACHABLE,
                activeColor = DisconnectedRed
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (reachability) {
                    ReachabilityState.UNKNOWN -> stringResource(R.string.stato_controllo)
                    ReachabilityState.REACHABLE -> stringResource(R.string.stato_connesso)
                    ReachabilityState.UNREACHABLE -> stringResource(R.string.stato_non_raggiungibile)
                },
                style = MaterialTheme.typography.labelSmall,
                color = when (reachability) {
                    ReachabilityState.UNKNOWN -> Color.Gray
                    ReachabilityState.REACHABLE -> ConnectedGreen
                    ReachabilityState.UNREACHABLE -> DisconnectedRed
                }
            )
        }
    }
}

@Composable
private fun StatusDot(active: Boolean, activeColor: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(if (active) activeColor else Color.Gray.copy(alpha = 0.3f))
    )
}

// ──────────────────── Edit/Add Dialog ────────────────────

@Composable
private fun DesktopEditDialog(
    desktop: DesktopServer?,
    onDismiss: () -> Unit,
    onSave: (DesktopServer) -> Unit,
    onDelete: (() -> Unit)?
) {
    val isNew = desktop == null
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(desktop?.name ?: "") }
    var host by remember { mutableStateOf(desktop?.host ?: "") }
    var portText by remember { mutableStateOf((desktop?.port ?: 8443).toString()) }
    var plainToken by remember {
        mutableStateOf(
            if (desktop != null && desktop.encryptedToken.isNotBlank()) {
                runCatching {
                    KeystoreHelper.decrypt(desktop.encryptedToken, desktop.tokenIv)
                }.getOrDefault("")
            } else ""
        )
    }

    var rdpEnabled by remember {
        mutableStateOf(desktop?.services?.contains(DesktopService.REMOTE_DESKTOP) ?: false)
    }
    var sshEnabled by remember {
        mutableStateOf(desktop?.services?.contains(DesktopService.SSH) ?: false)
    }
    var dbEnabled by remember {
        mutableStateOf(desktop?.services?.contains(DesktopService.DATABASE) ?: false)
    }

    var healthMessage by remember { mutableStateOf("") }
    var isTesting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (isNew) R.string.pref_nuovo else R.string.pref_modifica
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.pref_nome)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(R.string.pref_host)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.pref_porta)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = plainToken,
                    onValueChange = { plainToken = it },
                    label = { Text(stringResource(R.string.pref_token)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Services toggles
                SwitchRow(stringResource(R.string.pref_servizio_rdp), rdpEnabled) { rdpEnabled = it }
                SwitchRow(stringResource(R.string.pref_servizio_ssh), sshEnabled) { sshEnabled = it }
                SwitchRow(stringResource(R.string.pref_servizio_db), dbEnabled) { dbEnabled = it }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Test connection
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        healthMessage = ""
                        scope.launch {
                            val (enc, iv) = if (plainToken.isNotBlank())
                                KeystoreHelper.encrypt(plainToken) else "" to ""
                            val tempDesktop = DesktopServer(
                                id = desktop?.id ?: "",
                                name = name,
                                host = host,
                                port = portText.toIntOrNull() ?: 8443,
                                encryptedToken = enc,
                                tokenIv = iv
                            )
                            val result = HealthApi.check(tempDesktop)
                            healthMessage = when (result) {
                                is HealthResult.Success ->
                                    if (result.db) "✅ Server OK, DB OK"
                                    else "⚠️ Server OK, DB non raggiungibile"
                                is HealthResult.TokenInvalid -> "❌ Token non valido (401)"
                                is HealthResult.ServerUnreachable -> "❌ Server non raggiungibile"
                                is HealthResult.DbUnreachable -> "⚠️ Database non raggiungibile"
                            }
                            isTesting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = host.isNotBlank() && !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.pref_test_connessione))
                }

                if (healthMessage.isNotBlank()) {
                    Text(
                        text = healthMessage,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Delete button (only for existing desktops)
                if (!isNew && onDelete != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.pref_elimina))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val port = portText.toIntOrNull() ?: 8443
                    val services = buildSet {
                        if (rdpEnabled) add(DesktopService.REMOTE_DESKTOP)
                        if (sshEnabled) add(DesktopService.SSH)
                        if (dbEnabled) add(DesktopService.DATABASE)
                    }
                    val (enc, iv) = if (plainToken.isNotBlank())
                        KeystoreHelper.encrypt(plainToken) else "" to ""
                    val result = DesktopServer(
                        id = desktop?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        host = host,
                        port = port,
                        encryptedToken = enc,
                        tokenIv = iv,
                        services = services,
                        order = desktop?.order ?: 0
                    )
                    onSave(result)
                },
                enabled = name.isNotBlank() && host.isNotBlank()
            ) {
                Text(stringResource(R.string.pref_salva))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.annulla))
            }
        }
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
