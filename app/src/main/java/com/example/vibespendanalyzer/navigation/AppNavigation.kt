package com.example.vibespendanalyzer.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vibespendanalyzer.ui.AiSettingsScreen
import com.example.vibespendanalyzer.ui.DayDetailScreen
import com.example.vibespendanalyzer.ui.HistoryScreen
import com.example.vibespendanalyzer.ui.HomeScreen
import com.example.vibespendanalyzer.ui.SpendingCalendarScreen

object Routes {
    const val CALENDAR = "calendar"
    const val RECORD = "record"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val DAY_DETAIL = "day_detail/{year}/{month}/{day}"

    fun dayDetail(year: Int, month: Int, day: Int): String =
        "day_detail/$year/$month/$day"
}

/**
 * 导航结构：
 * - 启动页：消费日历 [CALENDAR]
 * - 日历 → AI 设置 [SETTINGS]
 * - 点击日期 → 当日明细 [DAY_DETAIL]
 * - FAB → 智能记账 [RECORD]
 * - 记账页 → 历史 [HISTORY]
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CALENDAR,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.CALENDAR) {
            SpendingCalendarScreen(
                onNavigateToRecord = {
                    navController.navigate(Routes.RECORD)
                },
                onNavigateToDayDetail = { year, month, day ->
                    navController.navigate(Routes.dayDetail(year, month, day))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        composable(Routes.SETTINGS) {
            AiSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.DAY_DETAIL,
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: return@composable
            val month = backStackEntry.arguments?.getInt("month") ?: return@composable
            val day = backStackEntry.arguments?.getInt("day") ?: return@composable
            DayDetailScreen(
                year = year,
                month = month,
                day = day,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.RECORD) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
