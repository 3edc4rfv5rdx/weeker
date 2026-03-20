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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.weeker.app.data.local.EventTemplateEntity
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import kotlinx.coroutines.flow.Flow

@Composable
fun TemplatesScreen(
    t: (String) -> String,
    templatesFlow: Flow<List<EventTemplateEntity>>,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onAllNotes: () -> Unit = {},
    onAbout: () -> Unit,
    onExit: () -> Unit,
    onAddTemplate: (String) -> Unit,
    onUpdateTemplate: (EventTemplateEntity, String) -> Unit,
    onDeleteTemplate: (EventTemplateEntity) -> Unit
) {
    val rawTemplates by templatesFlow.collectAsState(initial = emptyList())
    val templates = rawTemplates.sortedBy { it.title.lowercase() }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<EventTemplateEntity?>(null) }
    var deletingTemplate by remember { mutableStateOf<EventTemplateEntity?>(null) }
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
                    text = t("templates").titleCaseFirst(),
                    fontSize = 26.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            AppMenuButton(
                t = t,
                onSettings = onOpenSettings,
                onAllNotes = onAllNotes,
                onAbout = onAbout,
                onExit = onExit
            )
        }

        WeekerButton(
            text = t("add template"),
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        )

        if (templates.isEmpty()) {
            Text(text = t("no templates"), fontSize = 18.sp)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(templates, key = { _, tmpl -> tmpl.id }) { index, template ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index % 2 == 0) colorA else colorB
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = template.title,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editingTemplate = template }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = t("edit")
                            )
                        }
                        IconButton(onClick = { deletingTemplate = template }) {
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
        TemplateNameDialog(
            t = t,
            title = t("add template").titleCaseFirst(),
            initialName = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onAddTemplate(name)
                showAddDialog = false
            }
        )
    }

    val editing = editingTemplate
    if (editing != null) {
        TemplateNameDialog(
            t = t,
            title = t("edit template").titleCaseFirst(),
            initialName = editing.title,
            onDismiss = { editingTemplate = null },
            onConfirm = { name ->
                onUpdateTemplate(editing, name)
                editingTemplate = null
            }
        )
    }

    val deleting = deletingTemplate
    if (deleting != null) {
        ConfirmDeleteTemplateDialog(
            t = t,
            templateTitle = deleting.title,
            onDismiss = { deletingTemplate = null },
            onConfirm = {
                onDeleteTemplate(deleting)
                deletingTemplate = null
            }
        )
    }
}

@Composable
private fun TemplateNameDialog(
    t: (String) -> String,
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
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
                    text = title,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(t("template name")) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true
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
                        onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                        enabled = name.isNotBlank()
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmDeleteTemplateDialog(
    t: (String) -> String,
    templateTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
                    text = t("delete template").titleCaseFirst(),
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = templateTitle,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
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
                        text = t("delete"),
                        onClick = onConfirm
                    )
                }
            }
        }
    }
}
