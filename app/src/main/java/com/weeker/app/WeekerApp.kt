package com.weeker.app

import android.app.Activity
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weeker.app.core.theme.WeekerTheme
import com.weeker.app.data.repository.EventRepository
import com.weeker.app.navigation.Routes
import com.weeker.app.ui.components.WeekerBackButton
import com.weeker.app.ui.components.WeekerButton
import com.weeker.app.ui.screens.EventEditScreen
import com.weeker.app.ui.screens.OnboardingScreen
import com.weeker.app.ui.screens.SettingsScreen
import com.weeker.app.ui.screens.TodayScreen
import com.weeker.app.ui.screens.WeekScreen
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.compose.ui.platform.LocalContext

@Composable
fun WeekerApp(container: AppContainer) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var lastTodayBackAt by remember { mutableLongStateOf(0L) }

    val onboardingDoneState = produceState<Boolean?>(initialValue = null) {
        container.settingsRepository.onboardingDoneFlow.collect { value = it }
    }
    val selectedLanguagePref by container.settingsRepository.languageFlow.collectAsState(initial = null)
    val selectedThemePref by container.settingsRepository.themeFlow.collectAsState(initial = null)

    val defaultLanguage = remember { container.localizationManager.defaultLanguage() }
    val defaultThemeId = remember { container.themeManager.defaultThemeId() }
    val onboardingLanguage = remember {
        container.localizationManager.availableLanguages().firstOrNull() ?: defaultLanguage
    }
    val selectedLanguage = selectedLanguagePref ?: defaultLanguage
    val selectedThemeId = selectedThemePref ?: defaultThemeId
    val theme = remember(selectedThemeId) { container.themeManager.themeById(selectedThemeId) }

    fun t(key: String): String = container.localizationManager.text(key, selectedLanguage)
    fun goBack(): Unit {
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

    WeekerTheme(theme = theme) {
        val onboardingDone = onboardingDoneState.value ?: return@WeekerTheme
        val startDestination = if (onboardingDone) Routes.TODAY else Routes.ONBOARDING

        NavHost(navController = navController, startDestination = startDestination) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    t = ::t,
                    currentLanguage = selectedLanguagePref ?: onboardingLanguage,
                    currentTheme = selectedThemeId,
                    languages = container.localizationManager.availableLanguages(),
                    themes = container.themeManager.allThemes(),
                    onBack = ::goBack,
                    onSave = { language, themeId ->
                        scope.launch {
                            container.settingsRepository.setLanguage(language)
                            container.settingsRepository.setTheme(themeId)
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
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
                    onAddEvent = { navController.navigate(Routes.eventEditRoute(todayEpochDay)) },
                    onOpenWeek = {
                        val monday = EventRepository.mondayStart(today)
                        navController.navigate(Routes.weekRoute(monday))
                    },
                    onOpenWeekPicker = { navController.navigate(Routes.WEEK_PICKER) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(
                route = Routes.WEEK,
                arguments = listOf(navArgument(Routes.WEEK_ARG) { type = NavType.LongType })
            ) { backStackEntry ->
                val start = backStackEntry.arguments?.getLong(Routes.WEEK_ARG) ?: EventRepository.mondayStart(LocalDate.now())
                WeekScreen(
                    t = ::t,
                    weekStart = start,
                    eventsFlow = container.eventRepository.observeWeek(start),
                    onBack = ::goBack,
                    onToggleDone = { event, checked ->
                        scope.launch { container.eventRepository.toggleDone(event, checked) }
                    },
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
                val epochDay = backStackEntry.arguments?.getLong(Routes.EVENT_DAY_ARG) ?: LocalDate.now().toEpochDay()
                EventEditScreen(
                    t = ::t,
                    epochDay = epochDay,
                    onBack = ::goBack,
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
                    currentTheme = selectedThemeId,
                    languages = container.localizationManager.availableLanguages(),
                    themes = container.themeManager.allThemes(),
                    onBackArrow = ::goBack,
                    onSave = { language, themeId ->
                        scope.launch {
                            container.settingsRepository.setLanguage(language)
                            container.settingsRepository.setTheme(themeId)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.WEEK_PICKER) {
                WeekPickerScreen(
                    t = ::t,
                    onPick = { date ->
                        val monday = EventRepository.mondayStart(date)
                        navController.navigate(Routes.weekRoute(monday))
                    },
                    onBack = ::goBack
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerScreen(
    t: (String) -> String,
    onPick: (LocalDate) -> Unit,
    onBack: () -> Unit
) {
    var selectedEpochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val selectedMillis = remember(selectedEpochDay) {
        LocalDate.ofEpochDay(selectedEpochDay)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)

    LaunchedEffect(pickerState.selectedDateMillis) {
        val millis = pickerState.selectedDateMillis ?: return@LaunchedEffect
        selectedEpochDay = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toEpochDay()
    }

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
            WeekerBackButton(onClick = onBack)
            Text(text = t("select week"))
        }
        DatePicker(state = pickerState, showModeToggle = false)
        WeekerButton(text = t("open week"), onClick = { onPick(LocalDate.ofEpochDay(selectedEpochDay)) }, modifier = Modifier.fillMaxWidth())
        WeekerButton(text = t("cancel"), onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}
