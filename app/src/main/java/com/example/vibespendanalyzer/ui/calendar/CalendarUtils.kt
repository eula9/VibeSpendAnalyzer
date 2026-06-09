package com.example.vibespendanalyzer.ui.calendar

import com.example.vibespendanalyzer.data.ExpenseAnalytics
import com.example.vibespendanalyzer.data.local.ExpenseRecord
import java.util.Calendar
import java.util.Locale

fun buildMonthGridItems(
    year: Int,
    month: Int,
    dailyTotals: Map<Int, Double>
): List<CalendarGridItem> {
    val calendar = Calendar.getInstance(Locale.getDefault()).apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingEmpty = mondayFirstOffset(calendar.get(Calendar.DAY_OF_WEEK))

    val today = Calendar.getInstance()
    val isCurrentMonth =
        today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
    val todayDay = if (isCurrentMonth) today.get(Calendar.DAY_OF_MONTH) else -1

    val items = mutableListOf<CalendarGridItem>()
    repeat(leadingEmpty) { items.add(CalendarGridItem.Empty) }
    for (day in 1..daysInMonth) {
        items.add(
            CalendarGridItem.Day(
                dayOfMonth = day,
                totalAmount = dailyTotals[day] ?: 0.0,
                isToday = day == todayDay
            )
        )
    }
    return items
}

/** 以周一为第一列时，月初前的空白格数量。 */
private fun mondayFirstOffset(dayOfWeek: Int): Int = (dayOfWeek + 5) % 7

fun formatMonthTitle(year: Int, month: Int): String = "${year}年${month + 1}月"

fun formatCurrency(amount: Double, emptyAsZero: Boolean = false): String {
    if (amount <= 0.0 && !emptyAsZero) return "¥0"
    return if (amount % 1.0 == 0.0) {
        "¥${amount.toInt()}"
    } else {
        "¥${"%.1f".format(Locale.getDefault(), amount)}"
    }
}

fun formatDayAmount(amount: Double): String {
    if (amount <= 0.0) return ""
    return formatCurrency(amount)
}

fun formatDayDetailTitle(year: Int, month: Int, day: Int): String =
    "${year}年${month + 1}月${day}日"

fun loadDailyTotals(
    records: List<ExpenseRecord>,
    year: Int,
    month: Int
): Map<Int, Double> = ExpenseAnalytics.dailyTotalsForMonth(records, year, month)
