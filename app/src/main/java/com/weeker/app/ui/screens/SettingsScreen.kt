package com.weeker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weeker.app.core.theme.ThemeMode
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.titleCaseFirst

@Composable
fun SettingsScreen(
    t: (String) -> String,
    currentLanguage: String,
    currentMode: ThemeMode,
    languages: List<String>,
    onBackArrow: () -> Unit,
    onOpenSettings: () -> Unit,
    onAllNotes: () -> Unit = {},
    onBackup: () -> Unit,
    onBackupToCsv: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
    onTemplates: () -> Unit,
    allowEditPast: Boolean,
    onAllowEditPastChanged: (Boolean) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onModeChanged: (ThemeMode) -> Unit
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }

    val modeLabel = when (currentMode) {
        ThemeMode.LIGHT -> t("light").titleCaseFirst()
        ThemeMode.DARK -> t("dark").titleCaseFirst()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeekerBackButton(onClick = onBackArrow)
                Text(text = t("settings").titleCaseFirst(), fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            AppMenuButton(
                t = t,
                onSettings = onOpenSettings,
                onAllNotes = onAllNotes,
                onAbout = onAbout,
                onExit = onExit
            )
        }

        SettingsRow(
            label = t("language").titleCaseFirst(),
            value = currentLanguage,
            onClick = { showLanguageDialog = true }
        )

        SettingsRow(
            label = t("mode").titleCaseFirst(),
            value = modeLabel,
            onClick = { showModeDialog = true }
        )

        SettingsRow(
            label = t("templates").titleCaseFirst(),
            value = "",
            onClick = onTemplates
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAllowEditPastChanged(!allowEditPast) }
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = t("edit past days").titleCaseFirst(), fontSize = 20.sp)
            Switch(
                checked = allowEditPast,
                onCheckedChange = onAllowEditPastChanged
            )
        }

        SettingsRow(
            label = t("backup").titleCaseFirst(),
            value = "",
            onClick = onBackup
        )

        SettingsRow(
            label = t("backup to csv").titleCaseFirst(),
            value = "",
            onClick = onBackupToCsv
        )

        SettingsRow(
            label = t("restore").titleCaseFirst(),
            value = "",
            onClick = onRestore
        )
    }

    if (showLanguageDialog) {
        SelectDialog(
            title = t("language").titleCaseFirst(),
            options = languages,
            selected = currentLanguage,
            onSelect = {
                onLanguageChanged(it)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showModeDialog) {
        SelectDialog(
            title = t("mode").titleCaseFirst(),
            options = listOf(ThemeMode.LIGHT.id, ThemeMode.DARK.id),
            optionLabels = listOf(t("light").titleCaseFirst(), t("dark").titleCaseFirst()),
            selected = currentMode.id,
            onSelect = {
                onModeChanged(if (it == ThemeMode.LIGHT.id) ThemeMode.LIGHT else ThemeMode.DARK)
                showModeDialog = false
            },
            onDismiss = { showModeDialog = false }
        )
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", fontSize = 20.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = "  >", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SelectDialog(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    optionLabels: List<String>? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                options.forEachIndexed { index, option ->
                    val label = optionLabels?.getOrNull(index) ?: option
                    Card(
                        onClick = { onSelect(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (option == selected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}
