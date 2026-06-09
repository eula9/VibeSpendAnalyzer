package com.example.vibespendanalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibespendanalyzer.data.ExpenseAnalytics
import com.example.vibespendanalyzer.data.ExpenseRepositoryProvider
import com.example.vibespendanalyzer.data.local.ExpenseRecord
import com.example.vibespendanalyzer.ui.calendar.formatCurrency
import com.example.vibespendanalyzer.ui.calendar.formatDayDetailTitle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    year: Int,
    month: Int,
    day: Int,
    onNavigateBack: () -> Unit
) {
    val allRecords by ExpenseRepositoryProvider.allRecords.collectAsState(initial = emptyList())
    val dayRecords = remember(allRecords, year, month, day) {
        ExpenseAnalytics.recordsForDay(allRecords, year, month, day)
    }
    val scope = rememberCoroutineScope()
    val dayTotal = remember(dayRecords) {
        dayRecords.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }
    var recordPendingDelete by remember { mutableStateOf<ExpenseRecord?>(null) }

    recordPendingDelete?.let { pending ->
        AlertDialog(
            onDismissRequest = { recordPendingDelete = null },
            title = {
                Text(
                    text = "删除这笔消费？",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "将删除「${pending.content}」（¥${pending.amount}）。\n" +
                        "日历、历史与本月统计会同步更新。",
                    color = VibeStyles.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            ExpenseRepositoryProvider.deleteById(pending.id)
                        }
                        recordPendingDelete = null
                    }
                ) {
                    Text(
                        text = "删除",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { recordPendingDelete = null }) {
                    Text(text = "取消", color = VibeStyles.AccentBlue)
                }
            },
            shape = RoundedCornerShape(VibeStyles.CardRadiusMedium)
        )
    }

    Scaffold(
        containerColor = VibeStyles.PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = formatDayDetailTitle(year, month, day),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VibeStyles.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = VibeStyles.AccentBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VibeStyles.PageBackground
                )
            )
        }
    ) { innerPadding ->
        if (dayRecords.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = VibeStyles.ScreenHorizontalPadding),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(VibeStyles.CardRadiusMedium),
                    colors = CardDefaults.cardColors(containerColor = VibeStyles.CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "这一天还没有消费",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = VibeStyles.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击日历页的 + 记一笔吧。",
                            style = MaterialTheme.typography.bodyLarge,
                            color = VibeStyles.TextSecondary,
                            lineHeight = 26.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = VibeStyles.ScreenHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    DaySummaryCard(
                        total = dayTotal,
                        count = dayRecords.size
                    )
                }
                items(
                    items = dayRecords,
                    key = { it.id }
                ) { record ->
                    ExpenseItemCard(
                        record = record,
                        onDelete = { recordPendingDelete = record }
                    )
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(total: Double, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeStyles.CardRadiusMedium),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            VibeStyles.HeaderGradientStart,
                            VibeStyles.HeaderGradientEnd
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "当日总支出",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(total, emptyAsZero = true),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "共 $count 笔消费",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}
