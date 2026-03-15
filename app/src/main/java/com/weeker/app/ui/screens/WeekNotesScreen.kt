package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weeker.app.data.local.WeekNoteEntity
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.ConfirmDeleteNoteDialog
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeekNotesScreen(
    t: (String) -> String,
    weekStart: Long,
    notesFlow: Flow<List<WeekNoteEntity>>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onAllNotes: () -> Unit = {},
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
    onAddNote: (String) -> Unit,
    onUpdateNote: (WeekNoteEntity, String) -> Unit,
    onDeleteNote: (WeekNoteEntity) -> Unit
) {
    val notes by notesFlow.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<WeekNoteEntity?>(null) }
    var deletingNote by remember { mutableStateOf<WeekNoteEntity?>(null) }
    val weekRange = formatWeekRangeNotes(weekStart)
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val colorA = if (isLight) Color(0xFFFFF6CC) else Color(0xFF3A3520)
    val colorB = if (isLight) Color(0xFFE3F2FD) else Color(0xFF1C2D3A)

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
                Text(
                    text = t("notes").titleCaseFirst(),
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
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
        Text(
            text = weekRange,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        WeekerButton(
            text = t("add week note").titleCaseFirst(),
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        )
        if (notes.isEmpty()) {
            Text(text = t("no notes"), fontSize = 18.sp)
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(notes, key = { _, note -> note.id }) { index, note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index % 2 == 0) colorA else colorB
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.text,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editingNote = note }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = t("edit")
                            )
                        }
                        IconButton(onClick = { deletingNote = note }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = t("delete")
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        NoteEditDialog(
            t = t,
            initialText = "",
            titleKey = "add note",
            onDismiss = { showAddDialog = false },
            onConfirm = { text ->
                onAddNote(text)
                showAddDialog = false
            }
        )
    }

    val editing = editingNote
    if (editing != null) {
        NoteEditDialog(
            t = t,
            initialText = editing.text,
            titleKey = "edit note",
            onDismiss = { editingNote = null },
            onConfirm = { text ->
                onUpdateNote(editing, text)
                editingNote = null
            }
        )
    }

    val deleting = deletingNote
    if (deleting != null) {
        ConfirmDeleteNoteDialog(
            t = t,
            noteText = deleting.text,
            onDismiss = { deletingNote = null },
            onConfirm = {
                onDeleteNote(deleting)
                deletingNote = null
            }
        )
    }
}

@Composable
private fun NoteEditDialog(
    t: (String) -> String,
    initialText: String,
    titleKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = t(titleKey).titleCaseFirst(),
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(t("note text")) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    minLines = 3
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    WeekerButton(
                        text = t("cancel"),
                        onClick = onDismiss
                    )
                    WeekerButton(
                        text = t("save"),
                        onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                        enabled = text.isNotBlank()
                    )
                }
            }
        }
    }
}

private fun formatWeekRangeNotes(weekStart: Long): String {
    val start = LocalDate.ofEpochDay(weekStart)
    val end = start.plusDays(6)
    val startText = start.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    return if (start.year == end.year && start.month == end.month) {
        "$startText-${end.format(DateTimeFormatter.ofPattern("dd"))}"
    } else {
        "$startText-${end.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}"
    }
}
