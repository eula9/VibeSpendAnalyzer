package com.example.vibespendanalyzer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(record: ExpenseRecord): Long

    @Query("SELECT * FROM ExpenseRecord ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<ExpenseRecord>>

    @Query("DELETE FROM ExpenseRecord WHERE id = :id")
    suspend fun deleteById(id: Long)
}
