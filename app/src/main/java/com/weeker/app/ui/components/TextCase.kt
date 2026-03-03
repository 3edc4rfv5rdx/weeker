package com.weeker.app.ui.components

fun String.titleCaseFirst(): String {
    if (isEmpty()) return this
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
