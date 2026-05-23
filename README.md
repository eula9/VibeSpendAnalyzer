# AI 消费分析助手 (VibeSpendAnalyzer)

一款面向个人理财场景的 **Android 原生应用**：用「消费日历」一眼掌握支出节奏，用 **大模型** 对每一笔消费做幽默又理性的点评，并用 **Room 数据库** 让账单在重启后依然完整保留。

> 本项目面向技术展示与开源协作，**源码中不包含任何个人 API Key**，所有 AI 凭证均由终端用户在 App 内自行配置。

---

## 核心功能

### 专属消费日历看板（首页）

- 以 **月历网格** 展示每日消费汇总，热力色深浅反映当日支出高低
- 顶部展示 **本月总支出** 与记账笔数
- 点击任意日期进入 **当日明细**，支持查看与删除单笔记录
- 右下角 **FAB「+」** 快速进入智能记账

### 持久化历史账单

- 全部消费记录写入 **Room 本地数据库**（`vibe_spend.db`）
- **历史记录页** 按时间倒序展示金额、内容、时间与 AI 诊断全文
- **进程结束或重启 App 后数据不丢失**

### 动态 AI 模型配置面板

- 独立 **「AI 设置」** 页面（日历页右上角齿轮进入）
- 用户可自行填写并本地保存：
  - **API Key**（支持密码隐藏 / 显示切换）
  - **API 基础 URL**（默认 `https://api.deepseek.com/v1`，兼容 OpenAI 格式，可接 DeepSeek、Kimi 等）
  - **模型名称**（默认 `deepseek-chat`）
- 未配置 Key 时，记账分析会 **拦截网络请求** 并提示前往设置页配置

### 智能记账与分析

- 输入消费金额与内容，一键 **「记账并让 AI 分析」**
- 基于 **OpenAI 兼容 Chat Completions** 接口生成 100 字以内消费点评
- 分析成功后自动写入数据库，并清空输入框便于连续记账

---

## 技术栈亮点

| 领域 | 技术选型 | 说明 |
|------|----------|------|
| 语言 | **Kotlin** | 全项目 Kotlin 实现 |
| UI | **Jetpack Compose** | 声明式 UI、Material 3、统一 Vibe 视觉体系 |
| 导航 | **Navigation Compose** | 日历 / 记账 / 历史 / 设置 / 日明细多页面栈 |
| 持久化 | **Room + KSP** | `@Entity` / `Dao` / `AppDatabase`，编译期生成实现 |
| 异步 | **Kotlin Coroutines** | 网络请求、数据库写入在 IO 线程执行 |
| 响应式 | **Kotlin Flow** | `getAllRecords(): Flow<List<ExpenseRecord>>` |
| UI 绑定 | **collectAsState** | 日历、历史等页面随数据库变化 **自动刷新** |
| 网络 | **OkHttp** | 轻量 HTTPS 客户端，适配自定义 Base URL |
| 配置存储 | **SharedPreferences** | AI 凭证与业务数据隔离存储 |

### 架构要点（简述）

```
UI (Compose)
    ↓ collectAsState / suspend
ExpenseRepositoryProvider / AiSettingsStore
    ↓
Room (ExpenseDao)          SharedPreferences (AiSettings)
    ↓
SQLite 本地库             用户自填 API Key / URL / Model
```

---

## 隐私与安全设计

本项目 **完全遵循安全开源规范**：

- **不硬编码** 任何 API Key、Token 或私有 Base URL
- **不在** `build.gradle.kts`、`local.properties` 或 `BuildConfig` 中注入密钥
- 所有 AI 接口凭证均由用户在 App 端 **「AI 设置」** 页输入
- 凭证仅存于设备本地 **`SharedPreferences`（`ai_settings`）**，不上传、不进入版本库
- `.gitignore` 已排除 `local.properties`、`build/`、`secrets.properties` 等敏感或生成目录

> **建议**：若你曾在旧版本将 Key 写入配置文件，请在服务商控制台 **轮换密钥** 后再使用新 Key。

---

## 环境要求

- Android Studio（推荐最新稳定版）
- JDK 17+（与 Android Gradle Plugin 9.x 配套）
- Android SDK：**minSdk 24**，**targetSdk 36**
- 可连接互联网的设备或模拟器（调用 AI 接口时）

---

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/<你的用户名>/VibeSpendAnalyzer.git
cd VibeSpendAnalyzer
```

### 2. 配置本地 SDK 路径

复制示例文件并修改为你的 SDK 路径：

```bash
# Windows 示例：复制 local.properties.example 为 local.properties
```

`local.properties` **仅用于** 指定 `sdk.dir`，**不要** 在此填写 API Key。

### 3. 编译运行

1. 用 Android Studio 打开项目  
2. **Sync Project with Gradle Files**  
3. 连接真机或模拟器，点击 **Run**  

### 4. 配置 AI（首次使用必做）

1. 打开 App，进入首页 **消费日历**  
2. 点击右上角 **设置（齿轮）**  
3. 填写你的 **API Key**、**Base URL**、**模型名** → **保存设置**  
4. 点击 **+** 进入记账页，即可体验 AI 分析  

---

## 项目结构（主要源码）

```
app/src/main/java/com/example/vibespendanalyzer/
├── MainActivity.kt                 # 初始化 AiSettings / Room
├── AiChatClient.kt                 # OpenAI 兼容 HTTP 客户端
├── data/
│   ├── AiSettings*.kt              # SharedPreferences 配置
│   ├── ExpenseRepository.kt        # 消费记录仓库
│   ├── ExpenseAnalytics.kt         # 日历聚合统计
│   └── local/
│       ├── ExpenseRecord.kt        # Room @Entity
│       ├── ExpenseDao.kt
│       └── AppDatabase.kt
├── navigation/AppNavigation.kt     # 导航图
└── ui/
    ├── SpendingCalendarScreen.kt   # 消费日历（首页）
    ├── HomeScreen.kt               # 智能记账
    ├── HistoryScreen.kt            # 历史账单
    ├── AiSettingsScreen.kt         # AI 设置
    └── DayDetailScreen.kt          # 当日明细
```

---

## 开源与协作

欢迎 Issue / PR。提交代码前请确认：

- [ ] 未包含个人 API Key 或 `local.properties`
- [ ] 未提交 `app/build/` 等构建产物
- [ ] 已在真机验证：设置 → 记账 → 重启后数据仍在

---

## 许可证

本项目以学习与技术展示为目的开源；AI 服务的使用须遵守对应服务商条款。

---

**AI 消费分析助手** — 让每一笔消费，都花得明明白白。
