package com.weeker.app.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val TODAY = "today"
    const val SETTINGS = "settings"
    const val WEEK_PICKER = "week_picker"

    const val WEEK = "week/{weekStart}"
    const val WEEK_ARG = "weekStart"

    const val EVENT_EDIT = "event_edit/{epochDay}"
    const val EVENT_DAY_ARG = "epochDay"

    fun weekRoute(weekStart: Long) = "week/$weekStart"
    fun eventEditRoute(epochDay: Long) = "event_edit/$epochDay"
}
