package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.core.theme.AppThemeConfig
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton

@Composable
fun SettingsScreen(
    t: (String) -> String,
    currentLanguage: String,
    currentTheme: String,
    languages: List<String>,
    themes: List<AppThemeConfig>,
    onBackArrow: () -> Unit,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val selectedLanguage = remember { mutableStateOf(currentLanguage) }
    val selectedTheme = remember { mutableStateOf(currentTheme) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeekerBackButton(onClick = onBackArrow)
                Text(text = t("settings"), fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        item {
            Text(text = t("language"), fontSize = 24.sp)
        }

        items(languages) { language ->
            SelectOption(
                label = language,
                selected = selectedLanguage.value == language,
                onClick = { selectedLanguage.value = language }
            )
        }

        item {
            Text(text = t("theme"), fontSize = 24.sp)
        }

        items(themes) { theme ->
            SelectOption(
                label = theme.name,
                selected = selectedTheme.value == theme.id,
                onClick = { selectedTheme.value = theme.id }
            )
        }

        item {
            WeekerButton(
                text = t("save"),
                onClick = { onSave(selectedLanguage.value, selectedTheme.value) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            WeekerButton(text = t("cancel"), onClick = onBack, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SelectOption(label: String, selected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.padding(14.dp),
            fontSize = 20.sp
        )
    }
}
