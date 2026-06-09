package com.example.vibespendanalyzer.ui.calendar

/**
 * 日历网格中的单日单元，或月初前的空白占位。
 */
sealed class CalendarGridItem {
    data object Empty : CalendarGridItem()

    data class Day(
        val dayOfMonth: Int,
        val totalAmount: Double,
        val isToday: Boolean
    ) : CalendarGridItem()
}
