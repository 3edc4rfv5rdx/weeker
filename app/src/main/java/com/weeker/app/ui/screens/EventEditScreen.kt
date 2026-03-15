package com.weeker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.weeker.app.data.local.EventTemplateEntity
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
    onAllNotes: () -> Unit = {},
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit,
    templatesFlow: Flow<List<EventTemplateEntity>> = flowOf(emptyList())
) {
    val title = remember { mutableStateOf(initialTitle) }
    val note = remember { mutableStateOf(initialNote) }
    val isPastDay = epochDay < LocalDate.now().toEpochDay()
    val titleFocusRequester = remember { FocusRequester() }
    val noteFocusRequester = remember { FocusRequester() }
    val templates by templatesFlow.collectAsState(initial = emptyList())
    var activeField by remember { mutableStateOf("title") }
    var showTemplateMenu by remember { mutableStateOf(false) }

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val colorA = if (isLight) Color(0xFFFFF6CC) else Color(0xFF3A3520)
    val colorB = if (isLight) Color(0xFFE3F2FD) else Color(0xFF1C2D3A)

    LaunchedEffect(Unit) { titleFocusRequester.requestFocus() }

    fun focusActiveField() {
        if (activeField == "note") noteFocusRequester.requestFocus()
        else titleFocusRequester.requestFocus()
    }

    val activeText = if (activeField == "title") title.value else note.value
    val suggestions = if (activeText.isNotBlank() && templates.isNotEmpty()) {
        val matches = templates.filter { it.title.contains(activeText, ignoreCase = true) }
        // Close when text fully matches a template
        if (matches.size == 1 && matches[0].title.equals(activeText, ignoreCase = true)) emptyList() else matches
    } else {
        emptyList()
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
                WeekerBackButton(onClick = onBack)
                Text(text = t(if (isEdit) "edit" else "add event").titleCaseFirst(), fontSize = 30.sp)
            }
            AppMenuButton(
                t = t,
                onSettings = onOpenSettings,
                onAllNotes = onAllNotes,
                onBackup = onBackup,
                onRestore = onRestore,
                onAbout = onAbout,
                onExit = onExit
            )
        }
        Text(text = LocalDate.ofEpochDay(epochDay).toString(), fontSize = 20.sp)

        // Title field with autocomplete popup
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = title.value,
                onValueChange = { title.value = it },
                label = { Text(t("event title")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .onFocusChanged { if (it.isFocused) activeField = "title" },
                singleLine = true
            )
            if (activeField == "title" && suggestions.isNotEmpty()) {
                Popup(
                    offset = androidx.compose.ui.unit.IntOffset(0, 150),
                    properties = PopupProperties(focusable = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        TemplateSuggestionList(suggestions, colorA, colorB) {
                            title.value = it
                            focusActiveField()
                        }
                    }
                }
            }
        }

        // Comment field with autocomplete popup
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = note.value,
                onValueChange = { note.value = it },
                label = { Text(t("comment")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(noteFocusRequester)
                    .onFocusChanged { if (it.isFocused) activeField = "note" }
            )
            if (activeField == "note" && suggestions.isNotEmpty()) {
                Popup(
                    offset = androidx.compose.ui.unit.IntOffset(0, 150),
                    properties = PopupProperties(focusable = false)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        TemplateSuggestionList(suggestions, colorA, colorB) {
                            note.value = it
                            focusActiveField()
                        }
                    }
                }
            }
        }

        if (templates.isNotEmpty()) {
            Row {
                WeekerButton(
                    text = t("from template"),
                    onClick = { showTemplateMenu = true },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = showTemplateMenu,
                    onDismissRequest = { showTemplateMenu = false }
                ) {
                    templates.forEachIndexed { index, tmpl ->
                        DropdownMenuItem(
                            text = { Text(tmpl.title, fontSize = 18.sp) },
                            onClick = {
                                if (activeField == "note") {
                                    note.value = tmpl.title
                                } else {
                                    title.value = tmpl.title
                                }
                                showTemplateMenu = false
                                focusActiveField()
                            },
                            modifier = Modifier.background(if (index % 2 == 0) colorA else colorB)
                        )
                    }
                }
            }
        }
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

@Composable
private fun TemplateSuggestionList(
    suggestions: List<EventTemplateEntity>,
    colorA: Color,
    colorB: Color,
    onSelect: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        itemsIndexed(suggestions, key = { _, t -> t.id }) { index, tmpl ->
            Text(
                text = tmpl.title,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) colorA else colorB)
                    .clickable { onSelect(tmpl.title) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
