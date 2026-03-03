package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.core.theme.AppThemeConfig
import com.weeker.app.ui.components.WeekerButton

@Composable
fun OnboardingScreen(
    t: (String) -> String,
    currentLanguage: String,
    currentTheme: String,
    languages: List<String>,
    themes: List<AppThemeConfig>,
    onSave: (String, String) -> Unit
) {
    val selectedLanguage = remember { mutableStateOf(currentLanguage) }
    val selectedTheme = remember { mutableStateOf(currentTheme) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = t("onboarding title"), fontSize = 30.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        item {
            Text(text = t("language"), fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        items(languages) { language ->
            SelectCard(
                label = language,
                selected = selectedLanguage.value == language,
                onClick = { selectedLanguage.value = language }
            )
        }

        item {
            Text(text = t("theme"), fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        items(themes) { theme ->
            SelectCard(
                label = theme.name,
                selected = selectedTheme.value == theme.id,
                onClick = { selectedTheme.value = theme.id }
            )
        }

        item {
            WeekerButton(
                text = t("continue"),
                onClick = { onSave(selectedLanguage.value, selectedTheme.value) },
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
            modifier = Modifier.padding(14.dp),
            fontSize = 20.sp
        )
    }
}
