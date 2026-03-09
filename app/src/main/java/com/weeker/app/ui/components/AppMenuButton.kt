package com.weeker.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.background
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.sp

@Composable
fun AppMenuButton(
    t: (String) -> String,
    onSettings: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val menuBg = if (isLight) Color(0xFFE9DDF8) else Color(0xFF2D2640)
    val menuText = if (isLight) Color(0xFF111111) else Color(0xFFE8E0F0)

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = menuText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = androidx.compose.ui.Modifier.background(menuBg)
        ) {
            DropdownMenuItem(
                text = { Text(t("settings").titleCaseFirst(), fontSize = 20.sp, color = menuText) },
                onClick = {
                    expanded = false
                    onSettings()
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
            DropdownMenuItem(
                text = { Text(t("backup").titleCaseFirst(), fontSize = 20.sp, color = menuText) },
                onClick = {
                    expanded = false
                    onBackup()
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
            DropdownMenuItem(
                text = { Text(t("restore").titleCaseFirst(), fontSize = 20.sp, color = menuText) },
                onClick = {
                    expanded = false
                    onRestore()
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
            DropdownMenuItem(
                text = { Text(t("about").titleCaseFirst(), fontSize = 20.sp, color = menuText) },
                onClick = {
                    expanded = false
                    onAbout()
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
            DropdownMenuItem(
                text = { Text(t("exit").titleCaseFirst(), fontSize = 20.sp, color = menuText) },
                onClick = {
                    expanded = false
                    onExit()
                },
                colors = MenuDefaults.itemColors(textColor = menuText)
            )
        }
    }
}
