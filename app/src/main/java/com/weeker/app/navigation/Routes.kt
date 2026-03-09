package com.weeker.app.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val TODAY = "today"
    const val SETTINGS = "settings"
    const val WEEK_PICKER = "week_picker/{pickerMode}"
    const val PICKER_MODE_ARG = "pickerMode"

    fun weekPickerRoute(mode: String = "day") = "week_picker/$mode"

    const val WEEK = "week/{weekStart}"
    const val WEEK_ARG = "weekStart"

    const val DAY = "day/{epochDay}"
    const val DAY_ARG = "epochDay"

    const val EVENT_EDIT = "event_edit/{epochDay}"
    const val EVENT_DAY_ARG = "epochDay"

    const val EVENT_EDIT_ID = "event_edit_id/{eventId}"
    const val EVENT_ID_ARG = "eventId"

    fun eventEditByIdRoute(eventId: Long) = "event_edit_id/$eventId"

    fun dayRoute(epochDay: Long) = "day/$epochDay"
    fun weekRoute(weekStart: Long) = "week/$weekStart"
    fun eventEditRoute(epochDay: Long) = "event_edit/$epochDay"
}
