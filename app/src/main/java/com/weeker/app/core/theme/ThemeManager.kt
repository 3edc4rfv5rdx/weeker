package com.weeker.app.core.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject

class ThemeManager(context: Context) {
    private val defaultThemeId: String
    private val themes: Map<String, AppThemeConfig>

    init {
        val raw = context.assets.open("themes.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        defaultThemeId = root.optString("defaultTheme", "contrast_blue")
        val themeArray = root.getJSONArray("themes")
        val map = mutableMapOf<String, AppThemeConfig>()
        for (i in 0 until themeArray.length()) {
            val item = themeArray.getJSONObject(i)
            val config = AppThemeConfig(
                id = item.getString("id"),
                name = item.getString("name"),
                primary = parseColor(item.getString("primary")),
                onPrimary = parseColor(item.getString("onPrimary")),
                background = parseColor(item.getString("background")),
                onBackground = parseColor(item.getString("onBackground")),
                surface = parseColor(item.getString("surface")),
                onSurface = parseColor(item.getString("onSurface"))
            )
            map[config.id] = config
        }
        themes = map
    }

    fun defaultThemeId(): String = defaultThemeId

    fun allThemes(): List<AppThemeConfig> = themes.values.toList()

    fun themeById(id: String): AppThemeConfig = themes[id] ?: themes[defaultThemeId]
    ?: AppThemeConfig(
        id = "fallback",
        name = "fallback",
        primary = Color(0xFF0B57D0),
        onPrimary = Color.White,
        background = Color(0xFFF3F6FB),
        onBackground = Color(0xFF111111),
        surface = Color.White,
        onSurface = Color(0xFF111111)
    )

    private fun parseColor(hex: String): Color {
        val cleaned = hex.removePrefix("#")
        val value = cleaned.toLong(16)
        return if (cleaned.length == 6) {
            Color((0xFF000000 or value).toInt())
        } else {
            Color(value.toInt())
        }
    }
}

data class AppThemeConfig(
    val id: String,
    val name: String,
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color
)
