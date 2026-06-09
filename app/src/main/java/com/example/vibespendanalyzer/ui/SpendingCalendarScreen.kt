package com.example.vibespendanalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.vibespendanalyzer.data.ExpenseAnalytics
import com.example.vibespendanalyzer.data.ExpenseRepositoryProvider
import com.example.vibespendanalyzer.ui.calendar.CalendarGridItem
import com.example.vibespendanalyzer.ui.calendar.buildMonthGridItems
import com.example.vibespendanalyzer.ui.calendar.formatCurrency
import com.example.vibespendanalyzer.ui.calendar.formatDayAmount
import com.example.vibespendanalyzer.ui.calendar.formatMonthTitle
import java.util.Calendar

private val WeekdayLabels = listOf("一", "二", "三", "四", "五", "六", "日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendingCalendarScreen(
    onNavigateToRecord: () -> Unit,
    onNavigateToDayDetail: (year: Int, month: Int, day: Int) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val now = remember { Calendar.getInstance() }
    var displayYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) }

    val records by ExpenseRepositoryProvider.allRecords.collectAsState(initial = emptyList())
    val dailyTotals = remember(records, displayYear, displayMonth) {
        ExpenseAnalytics.dailyTotalsForMonth(records, displayYear, displayMonth)
    }
    val gridItems = remember(dailyTotals, displayYear, displayMonth) {
        buildMonthGridItems(displayYear, displayMonth, dailyTotals)
    }
    val maxDailySpend = dailyTotals.values.maxOrNull() ?: 0.0
    val monthTotal = remember(records, displayYear, displayMonth) {
        ExpenseAnalytics.monthlyTotal(records, displayYear, displayMonth)
    }
    val monthCount = remember(records, displayYear, displayMonth) {
        ExpenseAnalytics.monthlyRecordCount(records, displayYear, displayMonth)
    }

    Scaffold(
        containerColor = VibeStyles.PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "消费日历",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VibeStyles.TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VibeStyles.PageBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI 设置",
                            tint = VibeStyles.AccentBlue
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRecord,
                containerColor = VibeStyles.AccentBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记一笔"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = VibeStyles.ScreenHorizontalPadding)
        ) {
            MonthSelector(
                title = formatMonthTitle(displayYear, displayMonth),
                onPrevious = {
                    if (displayMonth == Calendar.JANUARY) {
                        displayYear--
                        displayMonth = Calendar.DECEMBER
                    } else {
                        displayMonth--
                    }
                },
                onNext = {
                    if (displayMonth == Calendar.DECEMBER) {
                        displayYear++
                        displayMonth = Calendar.JANUARY
                    } else {
                        displayMonth++
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            MonthlySummaryCard(
                total = monthTotal,
                recordCount = monthCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(VibeStyles.CardRadiusMedium),
                colors = CardDefaults.cardColors(containerColor = VibeStyles.CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    WeekdayHeaderRow()
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(bottom = 4.dp),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(
                            items = gridItems,
                            key = { index, item ->
                                when (item) {
                                    is CalendarGridItem.Empty -> "e-$displayYear-$displayMonth-$index"
                                    is CalendarGridItem.Day ->
                                        "d-$displayYear-$displayMonth-${item.dayOfMonth}"
                                }
                            }
                        ) { _, item ->
                            when (item) {
                                CalendarGridItem.Empty -> CalendarEmptyCell()
                                is CalendarGridItem.Day -> CalendarDayCell(
                                    day = item,
                                    maxDailySpend = maxDailySpend,
                                    onClick = {
                                        onNavigateToDayDetail(
                                            displayYear,
                                            displayMonth,
                                            item.dayOfMonth
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "点击日期查看明细 · 颜色越深消费越多 · + 记一笔",
                style = MaterialTheme.typography.labelMedium,
                color = VibeStyles.TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    total: Double,
    recordCount: Int
) {
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
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Text(
                text = "本月总支出",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(total, emptyAsZero = true),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (recordCount > 0) "共 $recordCount 笔消费" else "暂无消费记录",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun MonthSelector(
    title: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "上个月",
                tint = VibeStyles.AccentBlue
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = VibeStyles.TextPrimary
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "下个月",
                tint = VibeStyles.AccentBlue
            )
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        WeekdayLabels.forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = VibeStyles.TextMuted
            )
        }
    }
}

@Composable
private fun CalendarEmptyCell() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
    )
}

@Composable
private fun CalendarDayCell(
    day: CalendarGridItem.Day,
    maxDailySpend: Double,
    onClick: () -> Unit
) {
    val hasSpend = day.totalAmount > 0.0
    val backgroundColor = spendingHeatColor(day.totalAmount, maxDailySpend)
    val cellShape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(VibeStyles.CardRadiusSmall))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .then(
                if (day.isToday) {
                    Modifier.border(1.5.dp, VibeStyles.AccentBlue, cellShape)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (day.isToday) VibeStyles.AccentBlue else VibeStyles.TextPrimary,
                fontSize = 14.sp
            )
            if (hasSpend) {
                Text(
                    text = formatDayAmount(day.totalAmount),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = VibeStyles.AccentBlueDark.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** 按当月最高消费比例生成柔和热力背景色。 */
private fun spendingHeatColor(amount: Double, maxInMonth: Double): Color {
    if (amount <= 0.0 || maxInMonth <= 0.0) return Color.Transparent
    val ratio = (amount / maxInMonth).toFloat().coerceIn(0f, 1f)
    val alpha = 0.10f + ratio * 0.28f
    return VibeStyles.AccentBlue.copy(alpha = alpha)
}
