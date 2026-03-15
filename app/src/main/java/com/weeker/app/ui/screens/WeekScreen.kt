package com.weeker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.data.local.EventEntity
import com.weeker.app.data.local.WeekNoteEntity
import com.weeker.app.core.theme.WeekStateColor
import com.weeker.app.core.theme.WeekStatusColors
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.ConfirmDeleteNoteDialog
import com.weeker.app.ui.components.WarningToast
import com.weeker.app.ui.components.EventRow
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun WeekScreen(
    t: (String) -> String,
    weekStart: Long,
    eventsFlow: Flow<List<EventEntity>>,
    notesFlow: Flow<List<WeekNoteEntity>>,
    weekStatusColors: WeekStatusColors,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onAllNotes: () -> Unit = {},
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
    onToggleDone: (EventEntity, Boolean) -> Unit,
    onEditEvent: (EventEntity) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    onMoveEvent: (EventEntity) -> Unit,
    onCopyEvent: (EventEntity) -> Unit,
    onMoveEventUp: (EventEntity) -> Unit,
    onMoveEventDown: (EventEntity) -> Unit,
    onAddEvent: (Long) -> Unit,
    onOpenDay: (Long) -> Unit,
    onOpenToday: () -> Unit,
    onOpenWeekPicker: () -> Unit,
    onOpenNotes: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    var deletingEvent by remember { mutableStateOf<EventEntity?>(null) }
    var showWarning by remember { mutableStateOf(false) }
    val events by eventsFlow.collectAsState(initial = emptyList())
    val notes by notesFlow.collectAsState(initial = emptyList())
    val hasNotes = notes.isNotEmpty()
    val today = LocalDate.now()
    val todayEpochDay = today.toEpochDay()
    val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong()).toEpochDay()
    val currentWeekStateColor: WeekStateColor = when {
        weekStart < currentWeekStart -> weekStatusColors.past
        weekStart > currentWeekStart -> weekStatusColors.future
        else -> weekStatusColors.current
    }
    val weekRange = formatWeekRange(weekStart)
    val pastColor = weekStatusColors.past
    val currentColor = weekStatusColors.current
    val futureColor = weekStatusColors.future

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header: back, title+date (left), notes button, menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeekerBackButton(onClick = onBack)
            Box(
                modifier = Modifier
                    .background(currentWeekStateColor.container, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Column {
                    Text(
                        text = t("week").titleCaseFirst(),
                        fontSize = 26.sp,
                        color = currentWeekStateColor.content
                    )
                    Text(
                        text = weekRange,
                        fontSize = 16.sp,
                        color = currentWeekStateColor.content
                    )
                }
            }
            Box(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(currentWeekStateColor.container, CircleShape)
                    .clickable(onClick = onOpenNotes)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (hasNotes) "N!" else "N",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentWeekStateColor.content
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
        // Navigation buttons — compact
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            WeekerButton(
                text = t("prev week"),
                onClick = onPrevWeek,
                modifier = Modifier.weight(1f),
                containerColor = pastColor.container,
                contentColor = pastColor.content
            )
            WeekerButton(
                text = t("next week"),
                onClick = onNextWeek,
                modifier = Modifier.weight(1f),
                containerColor = futureColor.container,
                contentColor = futureColor.content
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            WeekerButton(
                text = t("today"),
                onClick = onOpenToday,
                modifier = Modifier.weight(1f),
                containerColor = currentColor.container,
                contentColor = currentColor.content
            )
            WeekerButton(
                text = t("calendar"),
                onClick = onOpenWeekPicker,
                modifier = Modifier.weight(1f),
                containerColor = currentWeekStateColor.container,
                contentColor = currentWeekStateColor.content
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items((0L..6L).toList()) { dayOffset ->
                val day = weekStart + dayOffset
                val dayEvents = events.filter { it.dateEpochDay == day }
                val dayLocalDate = LocalDate.ofEpochDay(day)
                val dayName = dayNameKey(dayLocalDate.dayOfWeek.value)
                val dayLabel = "${t(dayName).titleCaseFirst()} ${dayLocalDate.dayOfMonth}"
                val dayStateColor = when {
                    day < todayEpochDay -> pastColor
                    day > todayEpochDay -> futureColor
                    else -> currentColor
                }
                val canAdd = day >= todayEpochDay
                val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
                val dayEventColorA = if (isLight) Color(0xFFFFF6CC) else Color(0xFF3A3520)
                val dayEventColorB = if (isLight) Color(0xFFE3F2FD) else Color(0xFF1C2D3A)

                Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(dayStateColor.container, CircleShape)
                                    .clickable { onOpenDay(day) }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 22.sp,
                                    color = dayStateColor.content
                                )
                            }
                        }
                        AddCircleButton(
                            enabled = canAdd,
                            colors = dayStateColor,
                            onClick = { onAddEvent(day) }
                        )
                    }
                    if (dayEvents.isEmpty()) {
                        Text(text = t("no events"), fontSize = 18.sp)
                    }
                    val orderedDayEvents = dayEvents.sortedWith(compareBy<EventEntity> { it.isDone }.thenBy { it.sortOrder }.thenBy { it.id })
                    val firstDone = orderedDayEvents.indexOfFirst { it.isDone }
                    val undoneCount = if (firstDone == -1) orderedDayEvents.size else firstDone
                    orderedDayEvents.forEachIndexed { index, event -> key(event.id) {
                        EventRow(
                            event = event,
                            t = t,
                            onToggleDone = { checked -> onToggleDone(event, checked) },
                            onEdit = onEditEvent,
                            onDelete = if (day >= todayEpochDay) {{ deletingEvent = it }} else {{ showWarning = true }},
                            onMoveTo = onMoveEvent,
                            onCopyTo = onCopyEvent,
                            onMoveUp = if (!event.isDone) ({ onMoveEventUp(event) }) else null,
                            onMoveDown = if (!event.isDone) ({ onMoveEventDown(event) }) else null,
                            moveUpEnabled = !event.isDone && index > 0,
                            moveDownEnabled = !event.isDone && index < undoneCount - 1,
                            compact = true,
                            containerColor = if (index % 2 == 0) dayEventColorA else dayEventColorB
                        )
                    } }
                }
            }
        }
    }

    val deleting = deletingEvent
    if (deleting != null) {
        ConfirmDeleteNoteDialog(
            t = t,
            noteText = deleting.title + if (deleting.note.isNotBlank()) "\n${deleting.note}" else "",
            onDismiss = { deletingEvent = null },
            onConfirm = {
                onDeleteEvent(deleting)
                deletingEvent = null
            },
            titleKey = "delete event"
        )
    }

    if (showWarning) {
        WarningToast(
            text = t("cannot delete past events"),
            onDismiss = { showWarning = false }
        )
    }
}

@Composable
private fun AddCircleButton(enabled: Boolean, colors: WeekStateColor, onClick: () -> Unit) {
    val bg = if (enabled) colors.container else MaterialTheme.colorScheme.surface
    val fg = if (enabled) colors.content else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    Box(
        modifier = Modifier
            .background(bg, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(6.dp)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = fg)
    }
}

private fun formatWeekRange(weekStart: Long): String {
    val start = LocalDate.ofEpochDay(weekStart)
    val end = start.plusDays(6)
    val startText = start.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    return if (start.year == end.year && start.month == end.month) {
        "$startText-${end.format(DateTimeFormatter.ofPattern("dd"))}"
    } else {
        "$startText-${end.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))}"
    }
}

private fun dayNameKey(dayOfWeek: Int): String = when (dayOfWeek) {
    1 -> "monday"
    2 -> "tuesday"
    3 -> "wednesday"
    4 -> "thursday"
    5 -> "friday"
    6 -> "saturday"
    else -> "sunday"
}
