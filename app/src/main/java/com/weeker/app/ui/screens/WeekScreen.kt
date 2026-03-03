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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.data.local.EventEntity
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
    onBack: () -> Unit,
    onToggleDone: (EventEntity, Boolean) -> Unit,
    onAddEvent: (Long) -> Unit,
    onMoveUndone: () -> Unit,
    onOpenToday: () -> Unit,
    onOpenWeekPicker: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val events by eventsFlow.collectAsState(initial = emptyList())
    val todayEpochDay = LocalDate.now().toEpochDay()
    val weekRange = formatWeekRange(weekStart)

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
            WeekerBackButton(onClick = onBack)
            Text(text = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 34.sp, color = MaterialTheme.colorScheme.onBackground)) {
                    append(t("week").titleCaseFirst())
                    append(" ")
                }
                withStyle(SpanStyle(fontSize = 22.sp, color = MaterialTheme.colorScheme.onBackground)) {
                    append(weekRange)
                }
            })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            WeekerButton(text = t("previous week"), onClick = onPrevWeek, modifier = Modifier.weight(1f))
            WeekerButton(text = t("next week"), onClick = onNextWeek, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            WeekerButton(text = t("calendar"), onClick = onOpenWeekPicker, modifier = Modifier.weight(1f))
            WeekerButton(text = t("open today"), onClick = onOpenToday, modifier = Modifier.weight(1f))
        }
        WeekerButton(text = t("move undone"), onClick = onMoveUndone, modifier = Modifier.fillMaxWidth())

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
            items((0L..6L).toList()) { dayOffset ->
                val day = weekStart + dayOffset
                val dayEvents = events.filter { it.dateEpochDay == day }
                val dayName = dayNameKey(LocalDate.ofEpochDay(day).dayOfWeek.value)
                val dayDate = LocalDate.ofEpochDay(day).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
                val canAdd = day >= todayEpochDay

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)) {
                                append(t(dayName).titleCaseFirst())
                                append(" ")
                            }
                            withStyle(SpanStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)) {
                                append(dayDate)
                            }
                        })
                        AddCircleButton(enabled = canAdd, onClick = { onAddEvent(day) })
                    }
                    if (dayEvents.isEmpty()) {
                        Text(text = t("no events"), fontSize = 18.sp)
                    }
                    dayEvents.forEach { event ->
                        EventRow(event = event, onToggleDone = { checked -> onToggleDone(event, checked) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCircleButton(enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val fg = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    Box(
        modifier = Modifier
            .background(bg, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
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
