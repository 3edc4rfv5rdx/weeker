package com.weeker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weeker.app.data.local.EventEntity

@Composable
fun EventRow(
    event: EventEntity,
    doneLabel: String,
    onToggleDone: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = event.isDone,
            onCheckedChange = onToggleDone
        )
        Column {
            Text(text = event.title, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
            if (event.note.isNotBlank()) {
                Text(text = event.note, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(text = doneLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
