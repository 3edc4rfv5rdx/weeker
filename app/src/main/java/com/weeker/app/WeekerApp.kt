package com.weeker.app

import android.app.Activity
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
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
import androidx.compose.ui.window.DialogProperties
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
import com.weeker.app.data.local.EventTemplateEntity
import com.weeker.app.data.local.WeekNoteEntity
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
import com.weeker.app.ui.screens.AllNotesScreen
import com.weeker.app.ui.screens.TemplatesScreen
import com.weeker.app.ui.screens.WeekNotesScreen
import com.weeker.app.ui.screens.WeekScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    var lastExitTapAt by remember { mutableLongStateOf(0L) }

    var showAboutDialog by remember { mutableStateOf(false) }
    var moveEventTarget by remember { mutableStateOf<EventEntity?>(null) }
    var copyEventTarget by remember { mutableStateOf<EventEntity?>(null) }

    val onboardingDoneState = produceState<Boolean?>(initialValue = null) {
        container.settingsRepository.onboardingDoneFlow.collect { value = it }
    }
    val selectedLanguagePref by container.settingsRepository.languageFlow.collectAsState(initial = null)
    val selectedThemeModePref by container.settingsRepository.themeModeFlow.collectAsState(initial = null)

    val allowEditPast by container.settingsRepository.allowEditPastFlow.collectAsState(initial = false)
    val weeksWithNotes by container.weekNoteRepository.observeWeeksWithNotes().collectAsState(initial = emptySet())

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
    val isLight = selectedThemeMode == ThemeMode.LIGHT
    val menuBg = if (isLight) Color(0xFFE9DDF8) else Color(0xFF2D2640)
    val menuText = if (isLight) Color(0xFF111111) else Color(0xFFE8E0F0)
    val appTitle = "Weeker"
    val appAuthor = "Eugen"
    val appVersion = BuildConfig.VERSION_NAME
    val appBuild = BuildConfig.VERSION_CODE
    var showRestoreWarning by remember { mutableStateOf(false) }
    val restoreLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val fileName = queryDisplayName(context, uri)
        if (fileName.isNullOrBlank() || !fileName.lowercase(Locale.US).endsWith(".db")) {
            Toast.makeText(context, t("choose db backup file"), Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        scope.launch(Dispatchers.IO) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            runCatching {
                val backup = readBackupFromDb(context, uri)
                container.eventRepository.replaceAllEvents(backup.events)
                container.weekNoteRepository.replaceAllNotes(backup.notes)
                container.eventTemplateRepository.replaceAllTemplates(backup.templates)
                container.settingsRepository.restoreSettings(backup.settings)
            }.onSuccess {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, t("restore complete"), Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, t("restore failed"), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun goBack() {
        val popped = navController.popBackStack()
        if (!popped) {
            navController.navigate(Routes.TODAY) {
                launchSingleTop = true
            }
        }
    }

    fun exitFromAnyScreenByDoubleTap() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastExitTapAt <= 1500L) {
            (context as? Activity)?.finish()
        } else {
            lastExitTapAt = now
            Toast.makeText(context, t("press back again to exit"), Toast.LENGTH_SHORT).show()
        }
    }

    fun onSettingsFromMenu() {
        navController.navigate(Routes.SETTINGS) {
            launchSingleTop = true
        }
    }

    fun onAllNotesFromMenu() {
        navController.navigate(Routes.ALL_NOTES) {
            launchSingleTop = true
        }
    }

    fun onBackupFromMenu() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val events = container.eventRepository.exportEvents()
                val notes = container.weekNoteRepository.exportNotes()
                val templates = container.eventTemplateRepository.exportTemplates()
                val settings = container.settingsRepository.exportSettings()
                val folder = writePublicBackupFiles(context, events, notes, templates, settings)
                cleanupLegacyJsonBackups(context.filesDir)
                folder
            }.onSuccess { folderPath ->
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "${t("backup created")}: $folderPath", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                launch(Dispatchers.Main) {
                    Toast.makeText(context, t("backup failed"), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onRestoreFromMenu() {
        showRestoreWarning = true
    }

    fun onAboutFromMenu() {
        showAboutDialog = true
    }

    fun onExitFromMenu() {
        (context as? Activity)?.finish()
    }

    BackHandler {
        exitFromAnyScreenByDoubleTap()
    }

    LaunchedEffect(allowEditPast) {
        container.eventRepository.allowEditPast = allowEditPast
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
                    onBack = ::exitFromAnyScreenByDoubleTap,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
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
                val today = LocalDate.now()
                val todayEpochDay = today.toEpochDay()
                TodayScreen(
                    t = ::t,
                    eventsFlow = container.eventRepository.observeDay(todayEpochDay),
                    allowEditPast = allowEditPast,
                    onBack = ::exitFromAnyScreenByDoubleTap,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
                    onEditEvent = { target -> navController.navigate(Routes.eventEditByIdRoute(target.id)) },
                    onDeleteEvent = { target ->
                        scope.launch { container.eventRepository.deleteEvent(target) }
                    },
                    onMoveEvent = { moveEventTarget = it },
                    onCopyEvent = { copyEventTarget = it },
                    onMoveEventUp = { target ->
                        scope.launch { container.eventRepository.moveEventUpInToday(target) }
                    },
                    onMoveEventDown = { target ->
                        scope.launch { container.eventRepository.moveEventDownInToday(target) }
                    },
                    onAddEvent = { navController.navigate(Routes.eventEditRoute(todayEpochDay)) },
                    onOpenToday = {},
                    onOpenWeek = {
                        val monday = EventRepository.mondayStart(today)
                        navController.navigate(Routes.weekRoute(monday))
                    },
                    onOpenWeekPicker = { navController.navigate(Routes.weekPickerRoute("day")) },
                    onOpenNotes = {
                        val monday = EventRepository.mondayStart(today)
                        navController.navigate(Routes.weekNotesRoute(monday))
                    }
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
                    notesFlow = container.weekNoteRepository.observeByWeek(start),
                    weekStatusColors = palette.weekStatusColors,
                    allowEditPast = allowEditPast,
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
                    onEditEvent = { target -> navController.navigate(Routes.eventEditByIdRoute(target.id)) },
                    onDeleteEvent = { target ->
                        scope.launch { container.eventRepository.deleteEvent(target) }
                    },
                    onMoveEvent = { moveEventTarget = it },
                    onCopyEvent = { copyEventTarget = it },
                    onMoveEventUp = { target ->
                        scope.launch { container.eventRepository.moveEventUpInToday(target) }
                    },
                    onMoveEventDown = { target ->
                        scope.launch { container.eventRepository.moveEventDownInToday(target) }
                    },
                    onAddEvent = { day -> navController.navigate(Routes.eventEditRoute(day)) },
                    onOpenDay = { day -> navController.navigate(Routes.dayRoute(day)) },
                    onOpenToday = { navController.navigate(Routes.TODAY) },
                    onOpenWeekPicker = { navController.navigate(Routes.weekPickerRoute("week")) },
                    onOpenNotes = { navController.navigate(Routes.weekNotesRoute(start)) },
                    onPrevWeek = { navController.navigate(Routes.weekRoute(start - 7)) },
                    onNextWeek = { navController.navigate(Routes.weekRoute(start + 7)) }
                )
            }

            composable(
                route = Routes.DAY,
                arguments = listOf(navArgument(Routes.DAY_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val dayEpoch = backStackEntry.arguments?.getLong(Routes.DAY_ARG)
                    ?: LocalDate.now().toEpochDay()
                TodayScreen(
                    t = ::t,
                    epochDay = dayEpoch,
                    eventsFlow = container.eventRepository.observeDay(dayEpoch),
                    allowEditPast = allowEditPast,
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
                    onEditEvent = { target -> navController.navigate(Routes.eventEditByIdRoute(target.id)) },
                    onDeleteEvent = { target ->
                        scope.launch { container.eventRepository.deleteEvent(target) }
                    },
                    onMoveEvent = { moveEventTarget = it },
                    onCopyEvent = { copyEventTarget = it },
                    onMoveEventUp = { target ->
                        scope.launch { container.eventRepository.moveEventUpInToday(target) }
                    },
                    onMoveEventDown = { target ->
                        scope.launch { container.eventRepository.moveEventDownInToday(target) }
                    },
                    onAddEvent = { navController.navigate(Routes.eventEditRoute(dayEpoch)) },
                    onOpenToday = { navController.navigate(Routes.TODAY) },
                    onOpenWeek = {
                        val dayDate = LocalDate.ofEpochDay(dayEpoch)
                        val monday = EventRepository.mondayStart(dayDate)
                        navController.navigate(Routes.weekRoute(monday))
                    },
                    onOpenWeekPicker = { navController.navigate(Routes.weekPickerRoute("day")) },
                    onOpenNotes = {
                        val dayDate = LocalDate.ofEpochDay(dayEpoch)
                        val monday = EventRepository.mondayStart(dayDate)
                        navController.navigate(Routes.weekNotesRoute(monday))
                    }
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
                    allowEditPast = allowEditPast,
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onSave = { title, note ->
                        scope.launch {
                            container.eventRepository.addEvent(title, note, epochDay)
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    templatesFlow = container.eventTemplateRepository.observeAll()
                )
            }

            composable(
                route = Routes.EVENT_EDIT_ID,
                arguments = listOf(navArgument(Routes.EVENT_ID_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong(Routes.EVENT_ID_ARG) ?: 0L
                val eventState = produceState<EventEntity?>(initialValue = null, eventId) {
                    value = container.eventRepository.getEvent(eventId)
                }
                val event = eventState.value
                if (event != null) {
                    EventEditScreen(
                        t = ::t,
                        epochDay = event.dateEpochDay,
                        initialTitle = event.title,
                        initialNote = event.note,
                        isEdit = true,
                        allowEditPast = allowEditPast,
                        onBack = ::goBack,
                        onOpenSettings = ::onSettingsFromMenu,
                        onAllNotes = ::onAllNotesFromMenu,
                        onBackup = ::onBackupFromMenu,
                        onRestore = ::onRestoreFromMenu,
                        onAbout = ::onAboutFromMenu,
                        onExit = ::onExitFromMenu,
                        onSave = { title, note ->
                            scope.launch {
                                container.eventRepository.updateEvent(event, title, note)
                                navController.popBackStack()
                            }
                        },
                        onCancel = { navController.popBackStack() },
                        templatesFlow = container.eventTemplateRepository.observeAll()
                    )
                }
            }

            composable(
                route = Routes.WEEK_NOTES,
                arguments = listOf(navArgument(Routes.WEEK_NOTES_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val ws = backStackEntry.arguments?.getLong(Routes.WEEK_NOTES_ARG)
                    ?: EventRepository.mondayStart(LocalDate.now())
                WeekNotesScreen(
                    t = ::t,
                    weekStart = ws,
                    notesFlow = container.weekNoteRepository.observeByWeek(ws),
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onAddNote = { text ->
                        scope.launch { container.weekNoteRepository.addNote(ws, text) }
                    },
                    onUpdateNote = { note, text ->
                        scope.launch { container.weekNoteRepository.updateNote(note, text) }
                    },
                    onDeleteNote = { note ->
                        scope.launch { container.weekNoteRepository.deleteNote(note) }
                    }
                )
            }

            composable(Routes.ALL_NOTES) {
                AllNotesScreen(
                    t = ::t,
                    allNotesFlow = container.weekNoteRepository.observeAll(),
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onUpdateNote = { note, text ->
                        scope.launch { container.weekNoteRepository.updateNote(note, text) }
                    },
                    onDeleteNote = { note ->
                        scope.launch { container.weekNoteRepository.deleteNote(note) }
                    },
                    searchFlow = { query -> container.weekNoteRepository.search(query) }
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
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onTemplates = { navController.navigate(Routes.TEMPLATES) },
                    allowEditPast = allowEditPast,
                    onAllowEditPastChanged = { allow ->
                        scope.launch { container.settingsRepository.setAllowEditPast(allow) }
                    },
                    onLanguageChanged = { language ->
                        scope.launch { container.settingsRepository.setLanguage(language) }
                    },
                    onModeChanged = { mode ->
                        scope.launch { container.settingsRepository.setThemeMode(mode.id) }
                    }
                )
            }

            composable(Routes.TEMPLATES) {
                TemplatesScreen(
                    t = ::t,
                    templatesFlow = container.eventTemplateRepository.observeAll(),
                    onBack = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu,
                    onAddTemplate = { title ->
                        scope.launch { container.eventTemplateRepository.addTemplate(title) }
                    },
                    onUpdateTemplate = { template, title ->
                        scope.launch { container.eventTemplateRepository.updateTemplate(template, title) }
                    },
                    onDeleteTemplate = { template ->
                        scope.launch { container.eventTemplateRepository.deleteTemplate(template) }
                    }
                )
            }

            composable(
                route = Routes.WEEK_PICKER,
                arguments = listOf(navArgument(Routes.PICKER_MODE_ARG) { type = NavType.StringType })
            ) { backStackEntry ->
                val pickerMode = backStackEntry.arguments?.getString(Routes.PICKER_MODE_ARG) ?: "day"
                val pickerTitle = if (pickerMode == "week") t("week").titleCaseFirst() else t("day").titleCaseFirst()
                WeekPickerScreen(
                    t = ::t,
                    title = pickerTitle,
                    languageCode = selectedLanguage,
                    weeksWithNotes = weeksWithNotes,
                    onPick = { date ->
                        if (pickerMode == "week") {
                            val monday = EventRepository.mondayStart(date)
                            navController.navigate(Routes.weekRoute(monday))
                        } else {
                            navController.navigate(Routes.dayRoute(date.toEpochDay()))
                        }
                    },
                    onBackArrow = ::goBack,
                    onCancel = ::goBack,
                    onOpenSettings = ::onSettingsFromMenu,
                    onAllNotes = ::onAllNotesFromMenu,
                    onBackup = ::onBackupFromMenu,
                    onRestore = ::onRestoreFromMenu,
                    onAbout = ::onAboutFromMenu,
                    onExit = ::onExitFromMenu
                )
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Column {
                        Text(appTitle, color = menuText, fontSize = 24.sp)
                        Text(t("weekly planner").titleCaseFirst(), color = menuText, fontSize = 16.sp)
                    }
                },
                text = {
                    Text(
                        text = "${t("author").titleCaseFirst()}: $appAuthor\n" +
                            "${t("version").titleCaseFirst()}: $appVersion\n" +
                            "${t("build").titleCaseFirst()}: $appBuild",
                        color = menuText,
                        fontSize = 19.sp
                    )
                },
                containerColor = menuBg,
                confirmButton = {
                    WeekerButton(
                        text = t("ok").ifBlank { "OK" },
                        onClick = { showAboutDialog = false }
                    )
                }
            )
        }

        if (showRestoreWarning) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { showRestoreWarning = false }) {
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = t("restore").titleCaseFirst(),
                            fontSize = 22.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = t("all current data will be replaced with backup data"),
                            fontSize = 18.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            WeekerButton(
                                text = t("cancel"),
                                onClick = { showRestoreWarning = false }
                            )
                            WeekerButton(
                                text = t("restore"),
                                onClick = {
                                    showRestoreWarning = false
                                    restoreLauncher.launch(arrayOf("*/*"))
                                }
                            )
                        }
                    }
                }
            }
        }

        val moveTarget = moveEventTarget
        if (moveTarget != null) {
            MoveEventDateDialog(
                t = ::t,
                languageCode = selectedLanguage,
                titleKey = "move to",
                initialEpochDay = moveTarget.dateEpochDay,
                allowEditPast = allowEditPast,
                onDismiss = { moveEventTarget = null },
                onConfirm = { newEpochDay ->
                    scope.launch {
                        runCatching {
                            container.eventRepository.moveEventToDate(moveTarget, newEpochDay)
                        }.onFailure {
                            Toast.makeText(context, t("cannot add events in past"), Toast.LENGTH_SHORT).show()
                        }
                    }
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
                allowEditPast = allowEditPast,
                onDismiss = { copyEventTarget = null },
                onConfirm = { newEpochDay ->
                    scope.launch {
                        runCatching {
                            container.eventRepository.copyEventToDate(copyTarget, newEpochDay)
                        }.onFailure {
                            Toast.makeText(context, t("cannot add events in past"), Toast.LENGTH_SHORT).show()
                        }
                    }
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
    title: String,
    languageCode: String,
    weeksWithNotes: Set<Long> = emptySet(),
    onPick: (LocalDate) -> Unit,
    onBackArrow: () -> Unit,
    onCancel: () -> Unit,
    onOpenSettings: () -> Unit,
    onAllNotes: () -> Unit = {},
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit
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
                WeekerBackButton(onClick = onBackArrow)
                Text(text = title, fontSize = 26.sp)
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
        MondayDatePicker(
            languageCode = languageCode,
            epochDay = selectedEpochDay,
            onEpochDayChanged = { selectedEpochDay = it },
            weeksWithNotes = weeksWithNotes,
            modifier = Modifier.fillMaxWidth()
        )
        ConfirmCancelRow(
            t = t,
            onCancel = onCancel,
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
    allowEditPast: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedEpochDay by remember { mutableLongStateOf(initialEpochDay) }
    val canConfirm = selectedEpochDay >= LocalDate.now().toEpochDay() || allowEditPast

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        title = { Text(t(titleKey).titleCaseFirst()) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MondayDatePicker(
                    languageCode = languageCode,
                    epochDay = selectedEpochDay,
                    onEpochDayChanged = { selectedEpochDay = it },
                    modifier = Modifier.fillMaxWidth()
                )
                ConfirmCancelRow(
                    t = t,
                    onCancel = onDismiss,
                    onConfirm = { onConfirm(selectedEpochDay) },
                    confirmEnabled = canConfirm
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
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true
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
            modifier = Modifier.weight(1f),
            enabled = confirmEnabled
        )
    }
}

@Composable
private fun MondayDatePicker(
    languageCode: String,
    epochDay: Long,
    onEpochDayChanged: (Long) -> Unit,
    weeksWithNotes: Set<Long> = emptySet(),
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
            val rowMonday = first.minusDays(offset.toLong()).plusDays(week * 7L)
            val weekHasNotes = weeksWithNotes.contains(rowMonday.toEpochDay())
            val noteRowBg = if (weekHasNotes) {
                androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            } else Color.Transparent

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(noteRowBg, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            ) {
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

private fun writePublicBackupFiles(
    context: Context,
    events: List<EventEntity>,
    notes: List<WeekNoteEntity>,
    templates: List<EventTemplateEntity>,
    settings: Map<String, String>
): String {
    val dayFolder = "w" + SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    val timeStamp = SimpleDateFormat("HHmmss", Locale.US).format(Date())
    val dbFileName = "bak-$timeStamp.db"
    val csvFileName = "bak-$timeStamp.csv"
    val dbBytes = buildBackupDbBytes(context.cacheDir, events, notes, templates, settings)
    val csvBytes = buildBackupCsv(events).toByteArray(Charsets.UTF_8)

    writeToDocumentsWeeker(context, dayFolder, dbFileName, "application/vnd.sqlite3", dbBytes)
    writeToDocumentsWeeker(context, dayFolder, csvFileName, "text/csv", csvBytes)
    return "Documents/Weeker/$dayFolder"
}

private fun buildBackupDbBytes(
    tempDir: File,
    events: List<EventEntity>,
    notes: List<WeekNoteEntity>,
    templates: List<EventTemplateEntity>,
    settings: Map<String, String>
): ByteArray {
    val tempDb = File.createTempFile("weeker_backup_", ".db", tempDir)
    val db = SQLiteDatabase.openOrCreateDatabase(tempDb, null)
    try {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                note TEXT NOT NULL,
                dateEpochDay INTEGER NOT NULL,
                isDone INTEGER NOT NULL,
                sortOrder INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_events_dateEpochDay ON events(dateEpochDay)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_events_dateEpochDay_sortOrder ON events(dateEpochDay, sortOrder)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS week_notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                weekStartEpochDay INTEGER NOT NULL,
                text TEXT NOT NULL,
                sortOrder INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_week_notes_weekStartEpochDay ON week_notes(weekStartEpochDay)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS event_templates (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                sortOrder INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY NOT NULL,
                value TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.beginTransaction()
        try {
            events.forEach { event ->
                val values = ContentValues().apply {
                    put("id", event.id)
                    put("title", event.title)
                    put("note", event.note)
                    put("dateEpochDay", event.dateEpochDay)
                    put("isDone", if (event.isDone) 1 else 0)
                    put("sortOrder", event.sortOrder)
                }
                db.insertOrThrow("events", null, values)
            }
            notes.forEach { note ->
                val values = ContentValues().apply {
                    put("id", note.id)
                    put("weekStartEpochDay", note.weekStartEpochDay)
                    put("text", note.text)
                    put("sortOrder", note.sortOrder)
                }
                db.insertOrThrow("week_notes", null, values)
            }
            templates.forEach { tmpl ->
                val values = ContentValues().apply {
                    put("id", tmpl.id)
                    put("title", tmpl.title)
                    put("sortOrder", tmpl.sortOrder)
                }
                db.insertOrThrow("event_templates", null, values)
            }
            settings.forEach { (key, value) ->
                val values = ContentValues().apply {
                    put("key", key)
                    put("value", value)
                }
                db.insertOrThrow("settings", null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    } finally {
        db.close()
    }

    return try {
        tempDb.readBytes()
    } finally {
        tempDb.delete()
    }
}

private fun buildBackupCsv(events: List<EventEntity>): String {
    val sb = StringBuilder()
    sb.append("id,title,note,dateEpochDay,isDone,sortOrder\n")
    events.forEach { event ->
        sb.append(event.id).append(',')
        sb.append(csvEscape(event.title)).append(',')
        sb.append(csvEscape(event.note)).append(',')
        sb.append(event.dateEpochDay).append(',')
        sb.append(if (event.isDone) 1 else 0).append(',')
        sb.append(event.sortOrder).append('\n')
    }
    return sb.toString()
}

private fun csvEscape(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return "\"$escaped\""
}

private fun writeToDocumentsWeeker(
    context: Context,
    dayFolder: String,
    fileName: String,
    mimeType: String,
    bytes: ByteArray
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/Weeker/$dayFolder/"
        val collection = MediaStore.Files.getContentUri("external")
        val resolver = context.contentResolver

        findMediaFile(resolver = resolver, collection = collection, fileName = fileName, relativePath = relativePath)
            ?.let { resolver.delete(it, null, null) }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Cannot create backup file: $fileName")
        resolver.openOutputStream(uri)?.use { output ->
            output.write(bytes)
        } ?: throw IllegalStateException("Cannot write backup file: $fileName")
        val finalize = ContentValues().apply { put(MediaStore.MediaColumns.IS_PENDING, 0) }
        resolver.update(uri, finalize, null, null)
    } else {
        @Suppress("DEPRECATION")
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val targetDir = File(documentsDir, "Weeker/$dayFolder")
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetFile = File(targetDir, fileName)
        targetFile.writeBytes(bytes)
    }
}

private fun findMediaFile(
    resolver: android.content.ContentResolver,
    collection: Uri,
    fileName: String,
    relativePath: String
): Uri? {
    val projection = arrayOf(MediaStore.MediaColumns._ID)
    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
    val selectionArgs = arrayOf(fileName, relativePath)
    resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            return ContentUris.withAppendedId(collection, id)
        }
    }
    return null
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
        if (nameIdx >= 0 && cursor.moveToFirst()) {
            return cursor.getString(nameIdx)
        }
    }
    return uri.lastPathSegment?.substringAfterLast('/')
}

private fun cleanupLegacyJsonBackups(root: File) {
    val legacyDir = File(root, "backups")
    if (!legacyDir.exists()) return
    legacyDir.listFiles()
        ?.filter { it.isFile && it.extension.lowercase(Locale.US) == "json" }
        ?.forEach { it.delete() }
}

private data class BackupData(
    val events: List<EventEntity>,
    val notes: List<WeekNoteEntity>,
    val templates: List<EventTemplateEntity>,
    val settings: Map<String, String>
)

private fun readBackupFromDb(context: Context, backupUri: Uri): BackupData {
    val tempDb = File.createTempFile("weeker_restore_", ".db", context.cacheDir)
    try {
        context.contentResolver.openInputStream(backupUri)?.use { input ->
            tempDb.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Cannot open backup uri")

        val db = SQLiteDatabase.openDatabase(tempDb.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            val events = mutableListOf<EventEntity>()
            val cursor = db.query(
                "events",
                arrayOf("id", "title", "note", "dateEpochDay", "isDone", "sortOrder"),
                null, null, null, null,
                "dateEpochDay ASC, sortOrder ASC, id ASC"
            )
            cursor.use {
                val idIdx = it.getColumnIndexOrThrow("id")
                val titleIdx = it.getColumnIndexOrThrow("title")
                val noteIdx = it.getColumnIndexOrThrow("note")
                val dayIdx = it.getColumnIndexOrThrow("dateEpochDay")
                val doneIdx = it.getColumnIndexOrThrow("isDone")
                val sortIdx = it.getColumnIndexOrThrow("sortOrder")
                while (it.moveToNext()) {
                    events += EventEntity(
                        id = it.getLong(idIdx),
                        title = it.getString(titleIdx) ?: "",
                        note = it.getString(noteIdx) ?: "",
                        dateEpochDay = it.getLong(dayIdx),
                        isDone = it.getInt(doneIdx) != 0,
                        sortOrder = it.getInt(sortIdx)
                    )
                }
            }

            val notes = mutableListOf<WeekNoteEntity>()
            if (hasTable(db, "week_notes")) {
                val notesCursor = db.query(
                    "week_notes",
                    arrayOf("id", "weekStartEpochDay", "text", "sortOrder"),
                    null, null, null, null,
                    "weekStartEpochDay ASC, sortOrder ASC, id ASC"
                )
                notesCursor.use {
                    val idIdx = it.getColumnIndexOrThrow("id")
                    val weekIdx = it.getColumnIndexOrThrow("weekStartEpochDay")
                    val textIdx = it.getColumnIndexOrThrow("text")
                    val sortIdx = it.getColumnIndexOrThrow("sortOrder")
                    while (it.moveToNext()) {
                        notes += WeekNoteEntity(
                            id = it.getLong(idIdx),
                            weekStartEpochDay = it.getLong(weekIdx),
                            text = it.getString(textIdx) ?: "",
                            sortOrder = it.getInt(sortIdx)
                        )
                    }
                }
            }

            val templates = mutableListOf<EventTemplateEntity>()
            if (hasTable(db, "event_templates")) {
                val tmplCursor = db.query(
                    "event_templates",
                    arrayOf("id", "title", "sortOrder"),
                    null, null, null, null,
                    "sortOrder ASC, id ASC"
                )
                tmplCursor.use {
                    val idIdx = it.getColumnIndexOrThrow("id")
                    val titleIdx = it.getColumnIndexOrThrow("title")
                    val sortIdx = it.getColumnIndexOrThrow("sortOrder")
                    while (it.moveToNext()) {
                        templates += EventTemplateEntity(
                            id = it.getLong(idIdx),
                            title = it.getString(titleIdx) ?: "",
                            sortOrder = it.getInt(sortIdx)
                        )
                    }
                }
            }

            val settings = mutableMapOf<String, String>()
            if (hasTable(db, "settings")) {
                val settingsCursor = db.query(
                    "settings",
                    arrayOf("key", "value"),
                    null, null, null, null, null
                )
                settingsCursor.use {
                    val keyIdx = it.getColumnIndexOrThrow("key")
                    val valueIdx = it.getColumnIndexOrThrow("value")
                    while (it.moveToNext()) {
                        val k = it.getString(keyIdx) ?: continue
                        val v = it.getString(valueIdx) ?: continue
                        settings[k] = v
                    }
                }
            }

            return BackupData(events, notes, templates, settings)
        } finally {
            db.close()
        }
    } catch (e: SQLiteException) {
        throw IllegalStateException("Invalid backup db format", e)
    } finally {
        tempDb.delete()
    }
}

private fun hasTable(db: SQLiteDatabase, tableName: String): Boolean {
    db.rawQuery(
        "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
        arrayOf(tableName)
    ).use { return it.moveToFirst() }
}
