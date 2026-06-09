package com.example.vibespendanalyzer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibespendanalyzer.data.AiSettings
import com.example.vibespendanalyzer.data.AiSettingsDefaults
import com.example.vibespendanalyzer.data.AiSettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(onNavigateBack: () -> Unit) {
  val saved = remember { AiSettingsStore.load() }
  var apiKey by remember { mutableStateOf(saved.apiKey) }
  var baseUrl by remember { mutableStateOf(saved.baseUrl) }
  var modelName by remember { mutableStateOf(saved.modelName) }
  var apiKeyVisible by remember { mutableStateOf(false) }
  var showSaveSuccess by remember { mutableStateOf(false) }

  if (showSaveSuccess) {
    AlertDialog(
      onDismissRequest = { showSaveSuccess = false },
      title = {
        Text(text = "保存成功", fontWeight = FontWeight.SemiBold)
      },
      text = {
        Text(
          text = "配置保存成功！",
          color = VibeStyles.TextSecondary
        )
      },
      confirmButton = {
        TextButton(onClick = { showSaveSuccess = false }) {
          Text(text = "好的", color = VibeStyles.AccentBlue)
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
            text = "AI 设置",
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = VibeStyles.ScreenHorizontalPadding, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
            text = "模型接口配置",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = VibeStyles.TextPrimary
          )
          Text(
            text = "支持 OpenAI 兼容接口（DeepSeek、Kimi 等）。密钥仅保存在本机，不会上传到 GitHub。",
            style = MaterialTheme.typography.bodyMedium,
            color = VibeStyles.TextSecondary,
            lineHeight = 22.sp
          )

          OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API Key") },
            placeholder = { Text("sk-xxxxxxxx") },
            singleLine = true,
            visualTransformation = if (apiKeyVisible) {
              VisualTransformation.None
            } else {
              PasswordVisualTransformation()
            },
            trailingIcon = {
              IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                Icon(
                  imageVector = if (apiKeyVisible) {
                    Icons.Default.VisibilityOff
                  } else {
                    Icons.Default.Visibility
                  },
                  contentDescription = if (apiKeyVisible) "隐藏" else "显示",
                  tint = VibeStyles.TextMuted
                )
              }
            },
            shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
            colors = vibeTextFieldColors()
          )

          OutlinedTextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("API 基础 URL") },
            placeholder = { Text(AiSettingsDefaults.BASE_URL) },
            singleLine = true,
            shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
            colors = vibeTextFieldColors()
          )

          OutlinedTextField(
            value = modelName,
            onValueChange = { modelName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("模型名称") },
            placeholder = { Text(AiSettingsDefaults.MODEL_NAME) },
            singleLine = true,
            shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
            colors = vibeTextFieldColors()
          )

          Button(
            onClick = {
              AiSettingsStore.save(
                AiSettings(
                  apiKey = apiKey,
                  baseUrl = baseUrl.ifBlank { AiSettingsDefaults.BASE_URL },
                  modelName = modelName.ifBlank { AiSettingsDefaults.MODEL_NAME }
                )
              )
              showSaveSuccess = true
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp),
            shape = RoundedCornerShape(VibeStyles.CardRadiusSmall),
            colors = ButtonDefaults.buttonColors(
              containerColor = VibeStyles.AccentBlue,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
          ) {
            Text(
              text = "保存设置",
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      Text(
        text = "示例：DeepSeek 使用默认 URL 与 deepseek-chat；Kimi 可填 https://api.moonshot.cn/v1 与 moonshot-v1-8k",
        style = MaterialTheme.typography.labelMedium,
        color = VibeStyles.TextMuted,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}
