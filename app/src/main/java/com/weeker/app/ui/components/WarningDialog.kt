package com.weeker.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AppToast(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    durationMs: Long = 2000,
    onDismiss: () -> Unit
) {
    LaunchedEffect(text) {
        delay(durationMs)
        onDismiss()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                color = textColor,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun RedToast(text: String, onDismiss: () -> Unit) {
    AppToast(text = text, backgroundColor = Color(0xFFD32F2F), durationMs = 4000, onDismiss = onDismiss)
}

@Composable
fun GreenToast(text: String, onDismiss: () -> Unit) {
    AppToast(text = text, backgroundColor = Color(0xFF388E3C), onDismiss = onDismiss)
}

@Composable
fun OrangeToast(text: String, onDismiss: () -> Unit) {
    AppToast(text = text, backgroundColor = Color(0xFFFF9800), textColor = Color.Black, onDismiss = onDismiss)
}

@Composable
fun BlueToast(text: String, onDismiss: () -> Unit) {
    AppToast(text = text, backgroundColor = Color(0xFF1976D2), onDismiss = onDismiss)
}

// Keep for backward compatibility
@Composable
fun WarningToast(text: String, onDismiss: () -> Unit) {
    OrangeToast(text = text, onDismiss = onDismiss)
}
