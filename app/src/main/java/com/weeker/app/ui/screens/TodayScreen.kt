package com.weeker.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.data.local.EventEntity
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.EventRow
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(
    t: (String) -> String,
    epochDay: Long = LocalDate.now().toEpochDay(),
    eventsFlow: Flow<List<EventEntity>>,
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
    onAddEvent: () -> Unit,
    onOpenToday: () -> Unit,
    onOpenWeek: () -> Unit,
    onOpenWeekPicker: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val events by eventsFlow.collectAsState(initial = emptyList())
    val orderedEvents = events.sortedWith(compareBy<EventEntity> { it.isDone }.thenBy { it.sortOrder }.thenBy { it.id })
    val firstDoneIndex = orderedEvents.indexOfFirst { it.isDone }
    val undoneCount = if (firstDoneIndex == -1) orderedEvents.size else firstDoneIndex
    val displayDate = LocalDate.ofEpochDay(epochDay)
    val isToday = epochDay == LocalDate.now().toEpochDay()
    val todayText = displayDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val todayEventColorA = if (isLight) Color(0xFFFFF6CC) else Color(0xFF3A3520)
    val todayEventColorB = if (isLight) Color(0xFFE3F2FD) else Color(0xFF1C2D3A)

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
                if (isToday) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    WeekerBackButton(onClick = onBack)
                }
                val headerLabel = if (isToday) t("today").titleCaseFirst() else {
                    val dayKey = dayNameKey(displayDate.dayOfWeek.value)
                    t(dayKey).titleCaseFirst()
                }
                Text(text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 26.sp, color = MaterialTheme.colorScheme.onBackground)) {
                        append(headerLabel)
                        append(" ")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)) {
                        append(todayText)
                    }
                })
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
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            WeekerButton(text = t("week"), onClick = onOpenWeek, modifier = Modifier.weight(1f))
            WeekerButton(text = t("calendar"), onClick = onOpenWeekPicker, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            WeekerButton(text = t("notes"), onClick = onOpenNotes, modifier = Modifier.weight(1f))
            if (!isToday) {
                WeekerButton(text = t("today"), onClick = onOpenToday, modifier = Modifier.weight(1f))
            }
            WeekerButton(
                text = t("add"),
                onClick = onAddEvent,
                modifier = Modifier.weight(1f),
                enabled = epochDay >= LocalDate.now().toEpochDay()
            )
        }

        if (orderedEvents.isEmpty()) {
            Text(text = t("no events"), fontSize = 22.sp)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxSize()) {
            itemsIndexed(orderedEvents, key = { _, event -> event.id }) { index, event ->
                EventRow(
                    event = event,
                    t = t,
                    onToggleDone = { checked -> onToggleDone(event, checked) },
                    onEdit = onEditEvent,
                    onDelete = onDeleteEvent,
                    onMoveTo = onMoveEvent,
                    onCopyTo = onCopyEvent,
                    onMoveUp = if (!event.isDone) ({ onMoveEventUp(event) }) else null,
                    onMoveDown = if (!event.isDone) ({ onMoveEventDown(event) }) else null,
                    moveUpEnabled = !event.isDone && index > 0,
                    moveDownEnabled = !event.isDone && index < undoneCount - 1,
                    compact = true,
                    containerColor = if (index % 2 == 0) todayEventColorA else todayEventColorB,
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
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
