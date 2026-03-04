package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.core.theme.ThemeMode
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst

@Composable
fun OnboardingScreen(
    t: (String) -> String,
    currentLanguage: String,
    currentMode: ThemeMode,
    languages: List<String>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onSave: (String, ThemeMode) -> Unit
) {
    val selectedLanguage = remember { mutableStateOf(currentLanguage) }
    val selectedMode = remember { mutableStateOf(currentMode) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeekerBackButton(onClick = onBack)
                    Text(text = t("welcome").titleCaseFirst(), fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground)
                }
                AppMenuButton(
                    t = t,
                    onSettings = onOpenSettings,
                    onBackup = onBackup,
                    onRestore = onRestore,
                    onAbout = onAbout
                )
            }
        }

        item {
            Text(text = t("language").titleCaseFirst(), fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        items(languages) { language ->
            SelectCard(
                label = language,
                selected = selectedLanguage.value == language,
                onClick = { selectedLanguage.value = language }
            )
        }

        item {
            Text(text = t("mode").titleCaseFirst(), fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        item {
            SelectCard(
                label = t("light").titleCaseFirst(),
                selected = selectedMode.value == ThemeMode.LIGHT,
                onClick = { selectedMode.value = ThemeMode.LIGHT }
            )
        }

        item {
            SelectCard(
                label = t("dark").titleCaseFirst(),
                selected = selectedMode.value == ThemeMode.DARK,
                onClick = { selectedMode.value = ThemeMode.DARK }
            )
        }

        item {
            WeekerButton(
                text = t("continue"),
                onClick = { onSave(selectedLanguage.value, selectedMode.value) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SelectCard(label: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(10.dp),
            fontSize = 20.sp
        )
    }
}
