package com.example.vibespendanalyzer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibespendanalyzer.data.local.ExpenseRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseItemCard(
    record: ExpenseRecord,
    modifier: Modifier = Modifier,
    showTime: Boolean = true,
    timeLabel: String? = null,
    onDelete: (() -> Unit)? = null
) {
    val deleteTint = Color(0xFFEF4444)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeStyles.CardRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = VibeStyles.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "¥${record.amount}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = VibeStyles.AccentBlue,
                    modifier = Modifier.weight(1f)
                )
                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "删除",
                            tint = deleteTint
                        )
                    }
                }
            }
            Text(
                text = record.content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = VibeStyles.TextPrimary
            )
            if (showTime) {
                Text(
                    text = timeLabel ?: formatRecordTime(record.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    color = VibeStyles.TextMuted
                )
            }
            Text(
                text = record.aiAdvice,
                style = MaterialTheme.typography.bodyLarge,
                color = VibeStyles.TextSecondary,
                lineHeight = 26.sp
            )
        }
    }
}

fun formatRecordTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
