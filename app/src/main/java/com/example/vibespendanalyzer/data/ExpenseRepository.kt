package com.example.vibespendanalyzer.data

import android.content.Context
import com.example.vibespendanalyzer.data.local.AppDatabase
import com.example.vibespendanalyzer.data.local.ExpenseDao
import com.example.vibespendanalyzer.data.local.ExpenseRecord
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {

    val allRecords: Flow<List<ExpenseRecord>> = dao.getAllRecords()

    suspend fun insert(record: ExpenseRecord) {
        dao.insert(
            record.copy(id = 0)
        )
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }
}

/**
 * 全局消费记录仓库，在 [com.example.vibespendanalyzer.MainActivity] 启动时初始化。
 */
object ExpenseRepositoryProvider {

    private lateinit var repository: ExpenseRepository

    fun init(context: Context) {
        val dao = AppDatabase.getInstance(context).expenseDao()
        repository = ExpenseRepository(dao)
    }

    val allRecords: Flow<List<ExpenseRecord>>
        get() {
            checkInitialized()
            return repository.allRecords
        }

    suspend fun insert(record: ExpenseRecord) {
        checkInitialized()
        repository.insert(record)
    }

    suspend fun deleteById(id: Long) {
        checkInitialized()
        repository.deleteById(id)
    }

    private fun checkInitialized() {
        check(::repository.isInitialized) {
            "ExpenseRepositoryProvider 尚未初始化，请在 MainActivity.onCreate 中调用 init()"
        }
    }
}
