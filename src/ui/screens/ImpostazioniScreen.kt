package com.myhub.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.myhub.app.R
import com.myhub.app.data.settings.AppSettingsRepository
import com.myhub.app.ui.biometrics.BiometricHelper
import com.myhub.app.ui.theme.AppThemeMode
import kotlinx.coroutines.launch

@Composable
fun ImpostazioniScreen() {
    val context = LocalContext.current
    val repository = remember { AppSettingsRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    val currentTheme by repository.themeModeFlow.collectAsState(initial = AppThemeMode.SYSTEM)
    val notificationsEnabled by repository.notificationsEnabledFlow.collectAsState(initial = true)
    val failedThreshold by repository.failedLoginThresholdFlow.collectAsState(initial = 3)
    val biometricEnabled by repository.biometricLockEnabledFlow.collectAsState(initial = false)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }

    // Rileva la lingua per-app attuale
    val currentLocales = AppCompatDelegate.getApplicationLocales()
    val currentLanguageCode = if (!currentLocales.isEmpty) currentLocales[0]?.language ?: "it" else "it"
    val languageLabel = if (currentLanguageCode == "en") {
        stringResource(R.string.lingua_inglese)
    } else {
        stringResource(R.string.lingua_italiano)
    }

    val themeLabel = when (currentTheme) {
        AppThemeMode.SYSTEM -> stringResource(R.string.tema_sistema)
        AppThemeMode.LIGHT -> stringResource(R.string.tema_chiaro)
        AppThemeMode.DARK -> stringResource(R.string.tema_scuro)
    }

    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        // Sezione: Aspetto e Lingua
        Text(
            text = stringResource(R.string.sezione_aspetto),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.tema)) },
            supportingContent = { Text(themeLabel) },
            leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) },
            modifier = Modifier.clickable { showThemeDialog = true }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.lingua)) },
            supportingContent = { Text(languageLabel) },
            leadingContent = { Icon(Icons.Filled.Language, contentDescription = null) },
            modifier = Modifier.clickable { showLanguageDialog = true }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Sezione: Sicurezza e Notifiche
        Text(
            text = stringResource(R.string.sezione_sicurezza),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.notifiche)) },
            supportingContent = { Text(stringResource(R.string.notifiche_desc)) },
            leadingContent = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { isChecked ->
                        scope.launch { repository.setNotificationsEnabled(isChecked) }
                    }
                )
            }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.soglia_accessi)) },
            supportingContent = {
                Text(stringResource(R.string.soglia_accessi_desc, failedThreshold))
            },
            leadingContent = { Icon(Icons.Filled.Security, contentDescription = null) },
            modifier = Modifier.clickable { showThresholdDialog = true }
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.impronta)) },
            supportingContent = { Text(stringResource(R.string.impronta_desc)) },
            leadingContent = { Icon(Icons.Filled.Fingerprint, contentDescription = null) },
            trailingContent = {
                Switch(
                    checked = biometricEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            if (!BiometricHelper.canAuthenticate(context)) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.biometric_non_disponibile),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val activity = context as? AppCompatActivity
                                if (activity != null) {
                                    BiometricHelper.showBiometricPrompt(
                                        activity = activity,
                                        title = context.getString(R.string.biometric_prompt_title),
                                        subtitle = context.getString(R.string.biometric_prompt_subtitle),
                                        negativeButtonText = context.getString(R.string.annulla),
                                        onSuccess = {
                                            scope.launch { repository.setBiometricLockEnabled(true) }
                                        },
                                        onFailed = {
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.biometric_impronta_non_riconosciuta),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    scope.launch { repository.setBiometricLockEnabled(true) }
                                }
                            }
                        } else {
                            scope.launch { repository.setBiometricLockEnabled(false) }
                        }
                    }
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Sezione: Informazioni
        Text(
            text = stringResource(R.string.sezione_info),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        ListItem(
            headlineContent = { Text(stringResource(R.string.info_versione)) },
            supportingContent = { Text(versionName) },
            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) }
        )
    }

    // Dialog selezione Tema
    if (showThemeDialog) {
        val themeOptions = listOf(
            AppThemeMode.SYSTEM to stringResource(R.string.tema_sistema),
            AppThemeMode.LIGHT to stringResource(R.string.tema_chiaro),
            AppThemeMode.DARK to stringResource(R.string.tema_scuro)
        )

        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.tema)) },
            text = {
                Column {
                    themeOptions.forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentTheme == mode,
                                    onClick = {
                                        scope.launch { repository.setThemeMode(mode) }
                                        showThemeDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = currentTheme == mode,
                                onClick = null
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.chiudi))
                }
            }
        )
    }

    // Dialog selezione Lingua
    if (showLanguageDialog) {
        val languageOptions = listOf(
            "it" to stringResource(R.string.lingua_italiano),
            "en" to stringResource(R.string.lingua_inglese)
        )

        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.lingua)) },
            text = {
                Column {
                    languageOptions.forEach { (code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentLanguageCode == code,
                                    onClick = {
                                        AppCompatDelegate.setApplicationLocales(
                                            LocaleListCompat.forLanguageTags(code)
                                        )
                                        scope.launch { repository.setLanguage(code) }
                                        showLanguageDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = currentLanguageCode == code,
                                onClick = null
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.chiudi))
                }
            }
        )
    }

    // Dialog selezione Soglia avvisi accessi
    if (showThresholdDialog) {
        val thresholdOptions = listOf(1, 2, 3, 5, 10)

        AlertDialog(
            onDismissRequest = { showThresholdDialog = false },
            title = { Text(stringResource(R.string.soglia_accessi)) },
            text = {
                Column {
                    thresholdOptions.forEach { count ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = failedThreshold == count,
                                    onClick = {
                                        scope.launch { repository.setFailedLoginThreshold(count) }
                                        showThresholdDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = failedThreshold == count,
                                onClick = null
                            )
                            val label = if (count == 3) {
                                "$count (default)"
                            } else {
                                "$count"
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThresholdDialog = false }) {
                    Text(stringResource(R.string.chiudi))
                }
            }
        )
    }
}
