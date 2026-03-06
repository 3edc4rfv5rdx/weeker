package com.weeker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.data.local.EventEntity

@Composable
fun EventRow(
    event: EventEntity,
    t: (String) -> String,
    onToggleDone: (Boolean) -> Unit,
    onDelete: (EventEntity) -> Unit,
    onMoveTo: (EventEntity) -> Unit,
    onCopyTo: (EventEntity) -> Unit,
    containerColor: Color? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuBg = Color(0xFFE9DDF8)
    val menuText = Color(0xFF111111)
    val rowBg = containerColor ?: MaterialTheme.colorScheme.surface

    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(rowBg, RoundedCornerShape(14.dp))
                .pointerInput(event.id) {
                    detectTapGestures(onLongPress = { menuExpanded = true })
                }
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = event.isDone,
                onCheckedChange = onToggleDone
            )
            Column {
                Text(
                    text = event.title,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (event.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (event.note.isNotBlank()) {
                    Text(
                        text = event.note,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (event.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(menuBg)
        ) {
            DropdownMenuItem(
                text = { Text(t("delete").titleCaseFirst(), color = menuText, fontSize = 20.sp) },
                onClick = {
                    menuExpanded = false
                    onDelete(event)
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
            DropdownMenuItem(
                text = { Text(t("move to").titleCaseFirst(), color = menuText, fontSize = 20.sp) },
                onClick = {
                    menuExpanded = false
                    onMoveTo(event)
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
            DropdownMenuItem(
                text = { Text(t("copy to").titleCaseFirst(), color = menuText, fontSize = 20.sp) },
                onClick = {
                    menuExpanded = false
                    onCopyTo(event)
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
        }
    }
}
