package com.weeker.app

import android.app.Activity
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weeker.app.core.theme.ThemeMode
import com.weeker.app.core.theme.WeekerTheme
import com.weeker.app.data.local.EventEntity
import com.weeker.app.data.repository.EventRepository
import com.weeker.app.navigation.Routes
import com.weeker.app.ui.components.AppMenuButton
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.components.titleCaseFirst
import com.weeker.app.ui.screens.EventEditScreen
import com.weeker.app.ui.screens.OnboardingScreen
import com.weeker.app.ui.screens.SettingsScreen
import com.weeker.app.ui.screens.TodayScreen
import com.weeker.app.ui.screens.WeekScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun WeekerApp(container: AppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var lastTodayBackAt by remember { mutableLongStateOf(0L) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var moveEventTarget by remember { mutableStateOf<EventEntity?>(null) }
    var copyEventTarget by remember { mutableStateOf<EventEntity?>(null) }

    val onboardingDoneState = produceState<Boolean?>(initialValue = null) {
        container.settingsRepository.onboardingDoneFlow.collect { value = it }
    }
    val selectedLanguagePref by container.settingsRepository.languageFlow.collectAsState(initial = null)
    val selectedThemeModePref by container.settingsRepository.themeModeFlow.collectAsState(initial = null)

    val defaultLanguage = remember { container.localizationManager.defaultLanguage() }
    val defaultThemeId = remember { container.themeManager.defaultThemeId() }
    val defaultThemeMode = remember { container.themeManager.defaultMode() }
    val onboardingLanguage = remember {
        container.localizationManager.availableLanguages().firstOrNull() ?: defaultLanguage
    }
    val selectedLanguage = selectedLanguagePref ?: onboardingLanguage
    val selectedThemeMode = ThemeMode.fromId(selectedThemeModePref ?: defaultThemeMode.id)
    val theme = remember(defaultThemeId) { container.themeManager.themeById(defaultThemeId) }
    val palette = remember(theme, selectedThemeMode) { theme.palette(selectedThemeMode) }

    fun t(key: String): String = container.localizationManager.text(key, selectedLanguage)
    val menuBg = Color(0xFFE9DDF8)
    val menuText = Color(0xFF111111)

    fun goBack() {
        val popped = navController.popBackStack()
        if (!popped) {
            navController.navigate(Routes.TODAY) {
                launchSingleTop = true
            }
        }
    }

    fun exitFromTodayByDoubleBack() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastTodayBackAt <= 1500L) {
            (context as? Activity)?.finish()
        } else {
            lastTodayBackAt = now
            Toast.makeText(context, t("press back again to exit"), Toast.LENGTH_SHORT).show()
        }
    }

    fun onSettingsFromMenu() {
        navController.navigate(Routes.SETTINGS)
    }

    fun onBackupFromMenu() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val file = writeBackupFile(context.filesDir, container.eventRepository.exportEvents())
                file.name
            }.onSuccess { fileName ->
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "${t("backup created")}: $fileName", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, t("backup failed"), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onRestoreFromMenu() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val restored = restoreFromLatestBackup(context.filesDir)
                container.eventRepository.replaceAllEvents(restored)
            }.onSuccess {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, t("restore complete"), Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                launch(Dispatchers.Main) {
                    val message = if (it is NoSuchElementException) t("no backup files") else t("restore failed")
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onAboutFromMenu() {
        showAboutDialog = true
    }

    LaunchedEffect(selectedLanguage) {
        val localeTag = when (selectedLanguage) {
            "en" -> "en-GB"
            "uk" -> "uk-UA"
            "ru" -> "ru-RU"
            else -> selectedLanguage
        }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeTag))
    }

    WeekerTheme(theme = theme, mode = selectedThemeMode) {
        val onboardingDone = onboardingDoneState.value ?: return@WeekerTheme
        val startDestination = if (onboardingDone) Routes.TODAY else Routes.ONBOARDING

        NavHost(navController = navController, startDestination = startDestination) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    t = ::t,
                    currentLanguage = selectedLanguagePref ?: onboardingLanguage,
                    currentMode = selectedThemeMode,
                    languages = container.localizationManager.availableLanguages(),
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onSave = { language, mode ->
                        scope.launch {
                            container.settingsRepository.setLanguage(language)
                            container.settingsRepository.setThemeMode(mode.id)
                            container.settingsRepository.setOnboardingDone(true)
                            navController.navigate(Routes.TODAY) {
                                popUpTo(Routes.ONBOARDING) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.TODAY) {
                val today = remember { LocalDate.now() }
                val todayEpochDay = remember { today.toEpochDay() }
                TodayScreen(
                    t = ::t,
                    eventsFlow = container.eventRepository.observeDay(todayEpochDay),
                    onBack = ::exitFromTodayByDoubleBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
                    onDeleteEvent = { target ->
                        scope.launch { container.eventRepository.deleteEvent(target) }
                    },
                    onMoveEvent = { moveEventTarget = it },
                    onCopyEvent = { copyEventTarget = it },
                    onAddEvent = { navController.navigate(Routes.eventEditRoute(todayEpochDay)) },
                    onOpenWeek = {
                        val monday = EventRepository.mondayStart(today)
                        navController.navigate(Routes.weekRoute(monday))
                    },
                    onOpenWeekPicker = { navController.navigate(Routes.WEEK_PICKER) }
                )
            }

            composable(
                route = Routes.WEEK,
                arguments = listOf(navArgument(Routes.WEEK_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val start = backStackEntry.arguments?.getLong(Routes.WEEK_ARG)
                    ?: EventRepository.mondayStart(LocalDate.now())
                WeekScreen(
                    t = ::t,
                    weekStart = start,
                    eventsFlow = container.eventRepository.observeWeek(start),
                    weekStatusColors = palette.weekStatusColors,
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
                    onDeleteEvent = { target ->
                        scope.launch { container.eventRepository.deleteEvent(target) }
                    },
                    onMoveEvent = { moveEventTarget = it },
                    onCopyEvent = { copyEventTarget = it },
                    onAddEvent = { day -> navController.navigate(Routes.eventEditRoute(day)) },
                    onMoveUndone = {
                        scope.launch { container.eventRepository.moveUndoneToNextWeek(start) }
                    },
                    onOpenToday = { navController.navigate(Routes.TODAY) },
                    onOpenWeekPicker = { navController.navigate(Routes.WEEK_PICKER) },
                    onPrevWeek = { navController.navigate(Routes.weekRoute(start - 7)) },
                    onNextWeek = { navController.navigate(Routes.weekRoute(start + 7)) }
                )
            }

            composable(
                route = Routes.EVENT_EDIT,
                arguments = listOf(navArgument(Routes.EVENT_DAY_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val epochDay = backStackEntry.arguments?.getLong(Routes.EVENT_DAY_ARG)
                    ?: LocalDate.now().toEpochDay()
                EventEditScreen(
                    t = ::t,
                    epochDay = epochDay,
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onSave = { title, note ->
                        scope.launch {
                            container.eventRepository.addEvent(title, note, epochDay)
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    t = ::t,
                    currentLanguage = selectedLanguage,
                    currentMode = selectedThemeMode,
                    languages = container.localizationManager.availableLanguages(),
                    onBackArrow = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onSave = { language, mode ->
                        scope.launch {
                            container.settingsRepository.setLanguage(language)
                            container.settingsRepository.setThemeMode(mode.id)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.WEEK_PICKER) {
                WeekPickerScreen(
                    t = ::t,
                    languageCode = selectedLanguage,
                    onPick = { date ->
                        val monday = EventRepository.mondayStart(date)
                        navController.navigate(Routes.weekRoute(monday))
                    },
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu
                )
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(t("about").titleCaseFirst(), color = menuText, fontSize = 24.sp) },
                text = { Text(t("about text").titleCaseFirst(), color = menuText, fontSize = 19.sp) },
                containerColor = menuBg,
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(t("ok").ifBlank { "OK" }, color = menuText, fontSize = 18.sp)
                    }
                }
            )
        }

        val moveTarget = moveEventTarget
        if (moveTarget != null) {
            MoveEventDateDialog(
                t = ::t,
                languageCode = selectedLanguage,
                titleKey = "move to",
                initialEpochDay = moveTarget.dateEpochDay,
                onDismiss = { moveEventTarget = null },
                onConfirm = { newEpochDay ->
                    scope.launch { container.eventRepository.moveEventToDate(moveTarget, newEpochDay) }
                    moveEventTarget = null
                }
            )
        }

        val copyTarget = copyEventTarget
        if (copyTarget != null) {
            MoveEventDateDialog(
                t = ::t,
                languageCode = selectedLanguage,
                titleKey = "copy to",
                initialEpochDay = copyTarget.dateEpochDay,
                onDismiss = { copyEventTarget = null },
                onConfirm = { newEpochDay ->
                    scope.launch { container.eventRepository.copyEventToDate(copyTarget, newEpochDay) }
                    copyEventTarget = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerScreen(
    t: (String) -> String,
    languageCode: String,
    onPick: (LocalDate) -> Unit,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit
) {
    var selectedEpochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
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
                WeekerBackButton(onClick = onBack)
                Text(text = t("select week").titleCaseFirst())
            }
            AppMenuButton(
                t = t,
                onSettings = onOpenSettings,
                onBackup = onBackup,
                onRestore = onRestore,
                onAbout = onAbout
            )
        }
        MondayDatePicker(
            languageCode = languageCode,
            epochDay = selectedEpochDay,
            onEpochDayChanged = { selectedEpochDay = it },
            modifier = Modifier.fillMaxWidth()
        )
        ConfirmCancelRow(
            t = t,
            onCancel = onBack,
            onConfirm = { onPick(LocalDate.ofEpochDay(selectedEpochDay)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoveEventDateDialog(
    t: (String) -> String,
    languageCode: String,
    titleKey: String,
    initialEpochDay: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedEpochDay by remember { mutableLongStateOf(initialEpochDay) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(t(titleKey).titleCaseFirst()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MondayDatePicker(
                    languageCode = languageCode,
                    epochDay = selectedEpochDay,
                    onEpochDayChanged = { selectedEpochDay = it },
                    modifier = Modifier.fillMaxWidth()
                )
                ConfirmCancelRow(
                    t = t,
                    onCancel = onDismiss,
                    onConfirm = { onConfirm(selectedEpochDay) }
                )
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@Composable
private fun ConfirmCancelRow(
    t: (String) -> String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeekerButton(
            text = t("cancel").titleCaseFirst(),
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        )
        WeekerButton(
            text = t("ok").titleCaseFirst(),
            onClick = onConfirm,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MondayDatePicker(
    languageCode: String,
    epochDay: Long,
    onEpochDayChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var shownMonth by remember(epochDay) { mutableStateOf(LocalDate.ofEpochDay(epochDay).withDayOfMonth(1)) }
    var monthMenuExpanded by remember { mutableStateOf(false) }
    var yearMenuExpanded by remember { mutableStateOf(false) }
    val selectedDate = LocalDate.ofEpochDay(epochDay)
    val first = shownMonth.withDayOfMonth(1)
    val offset = first.dayOfWeek.value - 1
    val daysInMonth = shownMonth.lengthOfMonth()
    val monthLabels = monthLabels(languageCode)
    val currentMonthLabel = monthLabels[shownMonth.monthValue - 1]
    val yearRange = (shownMonth.year - 10)..(shownMonth.year + 10)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { shownMonth = shownMonth.minusMonths(1).withDayOfMonth(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("<") }
            Text(text = "$currentMonthLabel ${shownMonth.year}", fontSize = 24.sp)
            Button(
                onClick = { shownMonth = shownMonth.plusMonths(1).withDayOfMonth(1) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                )
            ) { Text(">") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { monthMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text(text = currentMonthLabel, fontSize = 24.sp) }
                DropdownMenu(
                    expanded = monthMenuExpanded,
                    onDismissRequest = { monthMenuExpanded = false }
                ) {
                    monthLabels.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(text = label, fontSize = 24.sp) },
                            onClick = {
                                shownMonth = shownMonth.withMonth(index + 1).withDayOfMonth(1)
                                monthMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { yearMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text(text = shownMonth.year.toString(), fontSize = 24.sp) }
                DropdownMenu(
                    expanded = yearMenuExpanded,
                    onDismissRequest = { yearMenuExpanded = false }
                ) {
                    yearRange.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(text = year.toString(), fontSize = 24.sp) },
                            onClick = {
                                shownMonth = shownMonth.withYear(year).withDayOfMonth(1)
                                yearMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            weekdayLabels(languageCode).forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    fontSize = 22.sp
                )
            }
        }

        repeat(6) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { day ->
                    val idx = week * 7 + day
                    val dayNum = idx - offset + 1
                    val inMonth = dayNum in 1..daysInMonth
                    val cellDate = if (inMonth) shownMonth.withDayOfMonth(dayNum) else null
                    val selected = cellDate == selectedDate

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = if (selected) androidx.compose.material3.MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable(enabled = inMonth) {
                                if (cellDate != null) onEpochDayChanged(cellDate.toEpochDay())
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (inMonth) {
                            Text(
                                text = dayNum.toString(),
                                fontSize = 28.sp,
                                color = if (selected) {
                                    androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                                } else {
                                    androidx.compose.material3.MaterialTheme.colorScheme.onBackground
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPickerHeader(epochDay: Long, languageCode: String): String {
    val date = LocalDate.ofEpochDay(epochDay)
    val day = date.dayOfMonth
    val year = date.year
    val dow = date.dayOfWeek.value
    val month = date.monthValue

    val weekRu = listOf("", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    val monthRu = listOf("", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    val weekUk = listOf("", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val monthUk = listOf("", "Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень")
    val weekEn = listOf("", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val monthEn = listOf("", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    val weekLabel: String
    val monthLabel: String
    when (languageCode) {
        "ru" -> {
            weekLabel = weekRu[dow]
            monthLabel = monthRu[month]
        }
        "uk" -> {
            weekLabel = weekUk[dow]
            monthLabel = monthUk[month]
        }
        else -> {
            weekLabel = weekEn[dow]
            monthLabel = monthEn[month]
        }
    }
    return "$year, $weekLabel $monthLabel $day."
}

private fun monthLabels(languageCode: String): List<String> {
    return when (languageCode) {
        "ru" -> listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
        "uk" -> listOf("Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень")
        else -> listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }
}

private fun weekdayLabels(languageCode: String): List<String> {
    return when (languageCode) {
        "ru" -> listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        "uk" -> listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
        else -> listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
    }
}

private fun writeBackupFile(root: File, events: List<EventEntity>): File {
    val dir = File(root, "backups")
    if (!dir.exists()) dir.mkdirs()
    val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File(dir, "weeker_backup_$stamp.json")

    val arr = JSONArray()
    events.forEach { e ->
        arr.put(
            JSONObject()
                .put("title", e.title)
                .put("note", e.note)
                .put("dateEpochDay", e.dateEpochDay)
                .put("isDone", e.isDone)
                .put("sortOrder", e.sortOrder)
        )
    }
    val rootObj = JSONObject().put("events", arr)
    file.writeText(rootObj.toString())
    return file
}

private fun restoreFromLatestBackup(root: File): List<EventEntity> {
    val dir = File(root, "backups")
    val latest = dir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }
        ?.maxByOrNull { it.lastModified() }
        ?: throw NoSuchElementException("No backup files")

    val json = JSONObject(latest.readText())
    val arr = json.getJSONArray("events")
    val restored = mutableListOf<EventEntity>()
    for (i in 0 until arr.length()) {
        val item = arr.getJSONObject(i)
        restored += EventEntity(
            id = 0,
            title = item.optString("title", ""),
            note = item.optString("note", ""),
            dateEpochDay = item.optLong("dateEpochDay"),
            isDone = item.optBoolean("isDone", false),
            sortOrder = item.optInt("sortOrder", i + 1)
        )
    }
    return restored
}
