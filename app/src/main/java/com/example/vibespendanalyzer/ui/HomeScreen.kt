package com.example.vibespendanalyzer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibespendanalyzer.AiChatClient
import com.example.vibespendanalyzer.data.AiSettingsStore
import com.example.vibespendanalyzer.data.MISSING_API_KEY_MESSAGE
import com.example.vibespendanalyzer.data.local.ExpenseRecord
import com.example.vibespendanalyzer.data.ExpenseRepositoryProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var amountText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var diagnosisText by remember { mutableStateOf(WelcomeMessage) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = VibeStyles.PageBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "智能记账",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = VibeStyles.TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回日历",
                            tint = VibeStyles.AccentBlue
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "查看历史",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VibeStyles.ScreenHorizontalPadding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeaderCard()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(VibeStyles.CardRadiusMedium),
                colors = CardDefaults.cardColors(containerColor = VibeStyles.CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "记一笔",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VibeStyles.TextPrimary
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("消费金额（元）") },
                        placeholder = { Text("例如：35.5") },
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
                        colors = vibeTextFieldColors()
                    )

                    OutlinedTextField(
                        value = contentText,
                        onValueChange = { contentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("消费内容") },
                        placeholder = { Text("例如：又喝了一杯生椰拿铁") },
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
                        colors = vibeTextFieldColors()
                    )

                    Button(
                        onClick = {
                            val amount = amountText.trim()
                            val content = contentText.trim()
                            when {
                                amount.isEmpty() || content.isEmpty() -> {
                                    diagnosisText = "请先填写消费金额和消费内容，再点击分析哦～"
                                }
                                amount.toDoubleOrNull() == null -> {
                                    diagnosisText = "金额格式不太对，请输入数字，例如：35 或 35.5"
                                }
                                else -> {
                                    val aiSettings = AiSettingsStore.load()
                                    if (aiSettings.apiKey.isBlank()) {
                                        diagnosisText = MISSING_API_KEY_MESSAGE
                                    } else {
                                        val prompt = buildAnalysisPrompt(amount, content)
                                        isLoading = true
                                        diagnosisText = LoadingMessage
                                        scope.launch {
                                            try {
                                                val reply = AiChatClient.chat(
                                                    baseUrl = aiSettings.baseUrl,
                                                    apiKey = aiSettings.apiKey,
                                                    model = aiSettings.modelName,
                                                    userPrompt = prompt
                                                )
                                                diagnosisText = reply
                                                ExpenseRepositoryProvider.insert(
                                                    ExpenseRecord(
                                                        amount = amount,
                                                        content = content,
                                                        aiAdvice = reply
                                                    )
                                                )
                                                amountText = ""
                                                contentText = ""
                                            } catch (e: Exception) {
                                                diagnosisText =
                                                    "分析失败：${e.message ?: "未知错误"}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VibeStyles.AccentBlue,
                            contentColor = Color.White,
                            disabledContainerColor = VibeStyles.AccentBlue.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.8f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp
                        )
                    ) {
                        Text(
                            text = if (isLoading) "分析中..." else "记账并让 AI 分析",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            DiagnosisCard(diagnosisText = diagnosisText, isLoading = isLoading)

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

private fun buildAnalysisPrompt(amount: String, content: String): String =
    "用户今天花了 $amount 元买了 $content，请以幽默且理性的理财专家视角，" +
        "用100字以内分析这笔消费是否合理，并给出建议。"

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeStyles.CardRadiusLarge),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = "AI",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "消费分析助手",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "智能记账 · 理性消费 · 每一笔钱都花得明白",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DiagnosisCard(diagnosisText: String, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(VibeStyles.CardRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = VibeStyles.CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "AI 诊断建议",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = VibeStyles.AccentBlueDark
            )

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = VibeStyles.AccentBlue,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = LoadingMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = VibeStyles.TextSecondary,
                        lineHeight = 26.sp
                    )
                }
            } else {
                Text(
                    text = diagnosisText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VibeStyles.TextSecondary,
                    lineHeight = 26.sp
                )
            }
        }
    }
}

@Composable
fun vibeTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = VibeStyles.AccentBlue,
    focusedLabelColor = VibeStyles.AccentBlue,
    cursorColor = VibeStyles.AccentBlue
)
