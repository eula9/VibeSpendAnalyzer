package com.example.vibespendanalyzer.data

import com.example.vibespendanalyzer.data.local.ExpenseRecord
import java.util.Calendar

/** 基于内存列表的日历/统计计算（数据源来自 Room Flow）。 */
object ExpenseAnalytics {

    fun dailyTotalsForMonth(
        records: List<ExpenseRecord>,
        year: Int,
        month: Int
    ): Map<Int, Double> {
        val totals = mutableMapOf<Int, Double>()
        val calendar = Calendar.getInstance()
        for (record in records) {
            calendar.timeInMillis = record.timestamp
            if (
                calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month
            ) {
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val amount = record.amount.toDoubleOrNull() ?: 0.0
                totals[day] = (totals[day] ?: 0.0) + amount
            }
        }
        return totals
    }

    fun monthlyTotal(records: List<ExpenseRecord>, year: Int, month: Int): Double =
        dailyTotalsForMonth(records, year, month).values.sum()

    fun monthlyRecordCount(records: List<ExpenseRecord>, year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        return records.count { record ->
            calendar.timeInMillis = record.timestamp
            calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month
        }
    }

    fun recordsForDay(
        records: List<ExpenseRecord>,
        year: Int,
        month: Int,
        day: Int
    ): List<ExpenseRecord> {
        val calendar = Calendar.getInstance()
        return records.filter { record ->
            calendar.timeInMillis = record.timestamp
            calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month &&
                calendar.get(Calendar.DAY_OF_MONTH) == day
        }.sortedByDescending { it.timestamp }
    }
}
