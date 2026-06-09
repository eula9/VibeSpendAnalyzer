package com.example.vibespendanalyzer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 消费记录表实体。
 */
@Entity(tableName = "ExpenseRecord")
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: String,
    val content: String,
    val aiAdvice: String,
    val timestamp: Long = System.currentTimeMillis()
)
