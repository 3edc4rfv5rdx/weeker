package com.weeker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.luminance
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
    onEdit: (EventEntity) -> Unit,
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
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val menuBg = if (isLight) Color(0xFFE9DDF8) else Color(0xFF2D2640)
    val menuText = if (isLight) Color(0xFF111111) else Color(0xFFE8E0F0)
    val rowBg = containerColor ?: MaterialTheme.colorScheme.surface
    val rowPadding = if (compact) 5.dp else 10.dp
    val titleSize = if (compact) 18.sp else 22.sp
    val noteSize = if (compact) titleSize else 16.sp
    val rowSpacing = if (compact) 5.dp else 10.dp

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
                if (event.note.isNotBlank()) {
                    Text(
                        text = event.note,
                        fontSize = noteSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (event.isDone) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = if (compact) 1 else Int.MAX_VALUE,
                        overflow = if (compact) TextOverflow.Ellipsis else TextOverflow.Clip
                    )
                }
            }
            if (onMoveUp != null || onMoveDown != null) {
                val canMove = (moveUpEnabled || moveDownEnabled)
                val handleTint = if (canMove)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                var accumulated by remember { mutableStateOf(0f) }
                val threshold = 40f
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = "Reorder",
                    tint = handleTint,
                    modifier = Modifier
                        .size(if (compact) 20.dp else 28.dp)
                        .pointerInput(event.id) {
                            detectTapGestures(onLongPress = {}, onTap = {})
                        }
                        .then(
                            if (canMove) {
                                Modifier.pointerInput(event.id, moveUpEnabled, moveDownEnabled) {
                                    detectVerticalDragGestures(
                                        onDragStart = { accumulated = 0f },
                                        onVerticalDrag = { _, dragAmount ->
                                            accumulated += dragAmount
                                            if (accumulated < -threshold && moveUpEnabled) {
                                                onMoveUp?.invoke()
                                                accumulated = 0f
                                            } else if (accumulated > threshold && moveDownEnabled) {
                                                onMoveDown?.invoke()
                                                accumulated = 0f
                                            }
                                        }
                                    )
                                }
                            } else Modifier
                        )
                )
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(menuBg)
        ) {
            DropdownMenuItem(
                text = { Text(t("edit").titleCaseFirst(), color = menuText, fontSize = 20.sp) },
                onClick = {
                    menuExpanded = false
                    onEdit(event)
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
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
