package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import java.time.LocalDate

@Composable
fun EventEditScreen(
    t: (String) -> String,
    epochDay: Long,
    initialTitle: String = "",
    initialNote: String = "",
    isEdit: Boolean = false,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val title = remember { mutableStateOf(initialTitle) }
    val note = remember { mutableStateOf(initialNote) }
    val isPastDay = epochDay < LocalDate.now().toEpochDay()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
                WeekerBackButton(onClick = onBack)
                Text(text = t(if (isEdit) "edit" else "add event").titleCaseFirst(), fontSize = 30.sp)
            }
            AppMenuButton(
                t = t,
                onSettings = onOpenSettings,
                onBackup = onBackup,
                onRestore = onRestore,
                onAbout = onAbout,
                onExit = onExit
            )
        }
        Text(text = LocalDate.ofEpochDay(epochDay).toString(), fontSize = 20.sp)
        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text(t("event title")) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            singleLine = true
        )
        OutlinedTextField(
            value = note.value,
            onValueChange = { note.value = it },
            label = { Text(t("comment")) },
            modifier = Modifier.fillMaxWidth()
        )
        if (isPastDay && !isEdit) {
            Text(text = t("cannot add events in past"), fontSize = 16.sp)
        }
        WeekerButton(
            text = t("save"),
            onClick = { if (title.value.isNotBlank()) onSave(title.value.trim(), note.value.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.value.isNotBlank() && (isEdit || !isPastDay)
        )
        WeekerButton(text = t("cancel"), onClick = onCancel, modifier = Modifier.fillMaxWidth())
    }
}
