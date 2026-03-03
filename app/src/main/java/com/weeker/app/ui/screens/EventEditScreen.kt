package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.ui.components.WeekerButton
import java.time.LocalDate

@Composable
fun EventEditScreen(
    t: (String) -> String,
    epochDay: Long,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val title = remember { mutableStateOf("") }
    val note = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = LocalDate.ofEpochDay(epochDay).toString(), fontSize = 20.sp)
        OutlinedTextField(
            value = title.value,
            onValueChange = { title.value = it },
            label = { Text(t("event title")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = note.value,
            onValueChange = { note.value = it },
            label = { Text(t("comment")) },
            modifier = Modifier.fillMaxWidth()
        )
        WeekerButton(
            text = t("save"),
            onClick = { if (title.value.isNotBlank()) onSave(title.value.trim(), note.value.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.value.isNotBlank()
        )
        WeekerButton(text = t("cancel"), onClick = onCancel, modifier = Modifier.fillMaxWidth())
    }
}
