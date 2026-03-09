package com.weeker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    moveUpEnabled: Boolean = false,
    moveDownEnabled: Boolean = false,
    compact: Boolean = false,
    containerColor: Color? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuBg = Color(0xFFE9DDF8)
    val menuText = Color(0xFF111111)
    val rowBg = containerColor ?: MaterialTheme.colorScheme.surface
    val rowPadding = if (compact) 3.dp else 10.dp
    val titleSize = if (compact) 15.sp else 22.sp
    val noteSize = if (compact) 12.sp else 16.sp
    val rowSpacing = if (compact) 4.dp else 10.dp

    Box {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(rowBg, RoundedCornerShape(if (compact) 8.dp else 14.dp))
                .pointerInput(event.id) {
                    detectTapGestures(onLongPress = { menuExpanded = true })
                }
                .padding(rowPadding),
            horizontalArrangement = Arrangement.spacedBy(rowSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (compact) {
                Checkbox(
                    checked = event.isDone,
                    onCheckedChange = onToggleDone,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Checkbox(
                    checked = event.isDone,
                    onCheckedChange = onToggleDone
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontSize = titleSize,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (event.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = if (compact) 1 else Int.MAX_VALUE,
                    overflow = if (compact) TextOverflow.Ellipsis else TextOverflow.Clip
                )
                if (event.note.isNotBlank() && !compact) {
                    Text(
                        text = event.note,
                        fontSize = noteSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (event.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
            if (onMoveUp != null || onMoveDown != null) {
                Column {
                    IconButton(onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null && moveUpEnabled) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null && moveDownEnabled) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
