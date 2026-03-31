package com.weeker.app.core.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONObject

enum class ThemeMode(val id: String) {
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromId(raw: String?): ThemeMode = if (raw == DARK.id) DARK else LIGHT
    }
}

class ThemeManager(context: Context) {
    private val defaultThemeId: String
    private val defaultMode: ThemeMode
    private val themes: Map<String, AppThemeConfig>

    init {
        val raw = context.assets.open("themes.json").bufferedReader().use { it.readText() }
        val root = JSONObject(raw)
        defaultThemeId = root.optString("defaultTheme", "contrast_blue")
        defaultMode = ThemeMode.fromId(root.optString("defaultMode", ThemeMode.LIGHT.id))
        val themeArray = root.getJSONArray("themes")
        val map = mutableMapOf<String, AppThemeConfig>()
        for (i in 0 until themeArray.length()) {
            val item = themeArray.getJSONObject(i)
            val config = AppThemeConfig(
                id = item.getString("id"),
                name = item.getString("name"),
                light = parsePalette(item.getJSONObject("light")),
                dark = parsePalette(item.getJSONObject("dark"))
            )
            map[config.id] = config
        }
        themes = map
    }

    fun defaultThemeId(): String = defaultThemeId

    fun defaultMode(): ThemeMode = defaultMode

    fun allThemes(): List<AppThemeConfig> = themes.values.toList()

    fun themeById(id: String): AppThemeConfig = themes[id] ?: themes[defaultThemeId]
    ?: AppThemeConfig(
        id = "fallback",
        name = "fallback",
        light = fallbackPalette(),
        dark = fallbackPalette()
    )

    private fun parsePalette(item: JSONObject): AppThemePalette {
        val weekStatus = item.getJSONObject("weekStatusColors")
        return AppThemePalette(
            primary = parseColor(item.getString("primary")),
            onPrimary = parseColor(item.getString("onPrimary")),
            background = parseColor(item.getString("background")),
            onBackground = parseColor(item.getString("onBackground")),
            surface = parseColor(item.getString("surface")),
            onSurface = parseColor(item.getString("onSurface")),
            weekStatusColors = WeekStatusColors(
                past = parseStateColor(weekStatus.getJSONObject("past")),
                current = parseStateColor(weekStatus.getJSONObject("current")),
                future = parseStateColor(weekStatus.getJSONObject("future"))
            )
        )
    }

    private fun parseStateColor(item: JSONObject): WeekStateColor {
        return WeekStateColor(
            container = parseColor(item.getString("container")),
            content = parseColor(item.getString("content"))
        )
    }

    private fun fallbackPalette(): AppThemePalette = AppThemePalette(
        primary = Color(0xFF0B57D0),
        onPrimary = Color.White,
        background = Color(0xFFF3F6FB),
        onBackground = Color(0xFF111111),
        surface = Color.White,
        onSurface = Color(0xFF111111),
        weekStatusColors = WeekStatusColors(
            past = WeekStateColor(container = Color(0xFFC4A674), content = Color(0xFF2A2118)),
            current = WeekStateColor(container = Color(0xFF5A9473), content = Color.White),
            future = WeekStateColor(container = Color(0xFF5A8DB5), content = Color.White)
        )
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
    val light: AppThemePalette,
    val dark: AppThemePalette
) {
    fun palette(mode: ThemeMode): AppThemePalette = if (mode == ThemeMode.DARK) dark else light
}

data class AppThemePalette(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val weekStatusColors: WeekStatusColors
)

data class WeekStatusColors(
    val past: WeekStateColor,
    val current: WeekStateColor,
    val future: WeekStateColor
)

data class WeekStateColor(
    val container: Color,
    val content: Color
)
