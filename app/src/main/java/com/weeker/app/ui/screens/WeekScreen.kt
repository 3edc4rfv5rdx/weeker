package com.weeker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.data.local.EventEntity
import com.weeker.app.ui.components.EventRow
import com.weeker.app.ui.components.WeekerButton
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Composable
fun WeekScreen(
    t: (String) -> String,
    weekStart: Long,
    eventsFlow: Flow<List<EventEntity>>,
    onToggleDone: (EventEntity, Boolean) -> Unit,
    onAddEvent: (Long) -> Unit,
    onMoveUndone: () -> Unit,
    onOpenToday: () -> Unit,
    onOpenWeekPicker: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val events by eventsFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = t("week"), fontSize = 34.sp, color = MaterialTheme.colorScheme.onBackground)
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

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(text = t(dayName), fontSize = 24.sp)
                    WeekerButton(text = t("add event"), onClick = { onAddEvent(day) }, modifier = Modifier.fillMaxWidth())
                    if (dayEvents.isEmpty()) {
                        Text(text = t("no events"), fontSize = 18.sp)
                    }
                    dayEvents.forEach { event ->
                        EventRow(event = event, doneLabel = t("done"), onToggleDone = { checked -> onToggleDone(event, checked) })
                    }
                }
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
