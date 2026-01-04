Memory Helper 在于：**它更像是一个“带有智能复习算法的待办清单（To-Do List）”**，而不是翻卡软件。

以下是为你量身定制的开发指南，涵盖了**架构设计**、**数据库模型**、**核心算法**和**关键技术栈**。

-----

### 一、 核心功能分析 (MVP 版本)

为了开发一个类似 **Memory Helper** 的全功能复习规划 App，我们需要将功能拆解得非常细致。为了方便你从开发角度理解，我将核心功能分为 **五大模块**：

---

### 一、 记忆对象管理模块 (The Content)
这是用户输入数据的地方，核心是“把什么东西放入复习计划”。

1.  **新建记忆条目 (Add Item)**
    * **标题与内容**：支持输入标题（如“新概念英语Lesson 1”）和备注内容。
    * **富文本支持**：支持插入图片（拍笔记）、Markdown 格式（加粗、列表）。
    * **归类系统**：
        * **记忆本 (Notebooks)**：类似文件夹的概念（如：考研政治、英语单词、工作技能）。
        * **标签 (Tags)**：辅助分类（如：重点、难点）。
2.  **条目列表展示**
    * 按“创建时间”排序。
    * 按“记忆本”筛选。
    * **搜索功能**：通过关键词搜索已添加的条目。
3.  **条目编辑与状态管理**
    * 修改内容。
    * **重置进度**：如果完全忘了，一键重置回“第1次复习”。
    * **归档/删除**：已完成或不再需要的条目放入回收站。

---

### 二、 复习曲线引擎模块 (The Algorithm)
这是 App 的心脏，决定了“什么时候提醒用户”。

1.  **内置曲线库**
    * 提供标准的艾宾浩斯曲线（如：5分钟、30分钟、12小时、1天、2天、4天、7天、15天）。
2.  **自定义曲线编辑器 (Custom Curve)**
    * 允许用户创建自己的周期（例如：“考前突击曲线”：10分钟、1小时、3小时、6小时）。
    * **单位选择**：支持分钟、小时、天。
3.  **复习时间计算逻辑 (核心算法)**
    * **动态计算**：下一次复习时间 = `上次完成时间` + `当前阶段间隔`。
    * **延期处理**：如果用户晚了3天复习，下一次提醒应该顺延，而不是堆积（智能调整）。
4.  **新记忆添加策略**
    * 支持“添加即复习”：添加时视为第1次复习。
    * 支持“仅添加”：添加后，等待用户手动点击“开始学习”才进入曲线周期。

---

### 三、 复习中心与交互模块 (The Interaction)
这是用户每天使用频率最高的界面。

1.  **今日待办列表 (To-Do List)**
    * **逾期任务 (Overdue)**：高亮显示，提示用户赶紧补救。
    * **今日任务**：按时间轴排序（如：上午10:00，下午2:00）。
    * **未来任务**：预览明后天的任务量。
2.  **复习操作交互**
    * **打钩完成**：最基础操作，点击后条目自动跳到下一个复习日期。
    * **模糊/忘记**：提供“忘记了”按钮，点击后逻辑可设为“重置当前阶段”或“退回上一阶段”。
    * **提前复习**：还没到时间，但用户想现在复习，支持手动打卡。
3.  **日历视图 (Calendar View)**
    * 在日历上用“小圆点”或“颜色深浅”展示哪天任务多，方便用户规划休息日。

---

### 四、 通知与提醒系统 (The Trigger)
Memory Helper 的核心价值在于“不漏掉任何一个复习点”。

1.  **精准推送 (Push Notifications)**
    * 利用 `AlarmManager` 实现准点提醒（即使 App 被杀后台）。
    * **通知内容**：显示“你有 3 个条目需要现在复习”。
2.  **提醒模式设置**
    * **智能合并**：不要每隔5分钟响一次，支持“每小时汇总推送”。
    * **免打扰时段 (DND)**：设置睡觉时间（如 23:00 - 07:00）不响铃，任务顺延到第二天早上。
3.  **桌面小组件 (Widgets)**
    * **列表组件**：在桌面直接看到今天要背什么，点击直接完成（无需打开 App）。
    * **热力图组件**：在桌面展示学习热度。

---

### 五、 数据统计与辅助功能 (The Utilities)

1.  **数据统计 (Statistics)**
    * **遗忘曲线图**：可视化展示用户的记忆持久度。
    * **学习热力图**：类似 GitHub 的 Contribution 图，展示每天的学习量。
2.  **数据备份与同步**
    * **本地备份**：导出 CSV 或 JSON 文件。
    * **云端同步** (进阶)：账号登录，多设备同步（需后端支持）。
3.  **主题系统**
    * 支持深色模式 (Dark Mode)。

---

### 💡 开发路线建议 (MVP 优先级)

如果你要一个人开发，不要试图一次性做完所有功能。建议按以下顺序迭代：

1.  **第一阶段 (原型机)**：
    * **核心：** 添加条目 + 标准艾宾浩斯算法 + 列表展示 + 打钩进入下一阶段。
    * **目的：** 跑通核心逻辑，确保时间计算不出错。

2.  **第二阶段 (可用版)**：
    * **核心：** `AlarmManager` 通知提醒 + 自定义曲线 + 简单的 Markdown 支持。
    * **目的：** 这是一个可以发布给核心用户测试的版本。

3.  **第三阶段 (精致版)**：
    * **核心：** 桌面小组件 (Widget) + 日历视图 + 统计图表 + UI 美化。
    * **目的：** 提升留存率和用户体验。

你打算先从**数据库结构**设计开始，还是先做**UI 原型**？

-----

### 二、 技术选型推荐 (Modern Android)

建议直接使用目前最主流的现代安卓开发技术栈，方便维护且性能好。
开发一个类似 **Memory Helper** 的应用，不仅需要能够运行，更需要**稳定**（提醒不漏）、**流畅**（列表滑动不卡）且**易于维护**。

鉴于你是从零开发，我强烈建议采用 Google 官方推崇的 **MAD (Modern Android Development)** 技术栈。这是目前安卓开发的“标准答案”。

以下是详细的技术选型清单，按架构分层列举：

-----

### 一、 核心语言与基础架构 (Foundation)

  * **开发语言：Kotlin**
      * **理由**：Google 首选语言，代码简洁，空安全（避免 NullPointerException），且拥有协程等强大的异步处理能力。
  * **架构模式：MVVM (Model-View-ViewModel)**
      * **理由**：将界面（UI）与数据逻辑分离。
          * `View` (UI) 只负责展示。
          * `ViewModel` 负责处理业务逻辑（比如计算下次复习时间）。
          * `Model` (Repository/Database) 负责存取数据。
  * **依赖注入：Hilt**
      * **理由**：虽然初期学起来稍难，但在管理数据库实例、全局配置、AlarmManager 实例时非常有用，能极大降低代码耦合度。

-----

### 二、 界面交互层 (UI Layer)

  * **UI 框架：Jetpack Compose**
      * **理由**：抛弃传统的 XML 布局。Compose 是声明式 UI，写列表（RecyclerView 的替代品）极其简单，代码量减少 50% 以上，且更容易实现复杂的动画效果。
  * **导航路由：Navigation Compose**
      * **理由**：专门配合 Compose 使用的页面跳转管理工具，支持单 Activity 多 Fragment/Screen 的模式。
  * **UI 规范：Material Design 3 (Material You)**
      * **理由**：Android 12+ 的设计规范，支持动态取色（App 颜色随用户壁纸变化），让你的 App 看起来非常原生、高级。

-----

### 三、 数据持久化层 (Data Layer)

这是记忆类 App 的命脉，数据绝对不能丢。

  * **本地数据库：Room Database (SQLite)**
      * **理由**：Google 官方对 SQLite 的封装。
          * 支持编译时 SQL 检查（写错 SQL 语句会报错）。
          * 完美配合 Kotlin Flow，当数据库数据变化时，UI 会自动刷新（无需手动刷新列表）。
          * **核心表设计**：`MemoryItem`（记忆条目）、`ReviewCurve`（复习曲线）、`ReviewLog`（复习日志）。
  * **轻量配置存储：DataStore (Preferences)**
      * **理由**：替代老旧的 `SharedPreferences`。用于存储用户的设置，比如“是否开启夜间模式”、“默认复习曲线ID”、“用户名”等。

-----

### 四、 核心功能组件 (Core Features Tech)

这里是实现 Memory Helper 核心逻辑的关键技术。

  * **精准提醒：AlarmManager**
      * **关键 API**：`setExactAndAllowWhileIdle()`
      * **理由**：**必须用这个！** WorkManager 在省电模式下会延迟执行，只有 AlarmManager 能做到“设定 10:00 响，就绝不拖到 10:05”。
  * **后台任务：WorkManager**
      * **用途**：用于非精准时间的任务。例如：每天凌晨 3 点自动清理回收站、自动备份数据、生成每日统计报表。
  * **桌面小组件：Jetpack Glance**
      * **理由**：使用 Compose 的语法来编写 App Widget。相比传统的 RemoteViews，Glance 开发效率高太多了，可以轻松做出好看的桌面复习列表。

-----

### 五、 第三方功能库推荐 (Libraries)

为了不重复造轮子，这些开源库能帮你节省大量时间。

  * **富文本/Markdown 渲染**
      * **选型**：`com.halilibo.compose-richtext` 或 `jeziellago/compose-markdown`
      * **用途**：让用户在备注里写 Markdown（加粗、列表、代码块）并渲染出来。
  * **图表统计**
      * **选型**：**Vico** (强烈推荐)
      * **理由**：专门为 Jetpack Compose 设计的轻量级图表库，非常适合画“艾宾浩斯遗忘曲线”和“热力图”。相比老牌的 MPAndroidChart，Vico 更现代、包体积更小。
  * **日历视图**
      * **选型**：**Kalendar** 或 **Compose-Calendar**
      * **用途**：展示月视图，并在日期下打点显示当天的复习量。
  * **JSON 解析**
      * **选型**：**Kotlin Serialization**
      * **理由**：比 Gson 更快，更适合 Kotlin，主要用于数据备份（导出为 JSON 文件）和恢复。

-----

### 六、 开发工具与环境 (Dev Tools)

  * **IDE**：Android Studio Ladybug (或最新稳定版)。
  * **版本控制**：Git (托管到 GitHub 或 Gitee)。
  * **数据库调试**：**App Inspection** (Android Studio 自带工具，可以直接实时查看、修改 Room 数据库的内容，调试算法时神器)。

-----

### 总结：你的 `build.gradle` 核心依赖预览

如果我帮你写配置文件，大概会包含这些核心库：

```kotlin
dependencies {
    // 基础 UI
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3") // Material Design 3

    // 导航
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 数据库 Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1") // 使用 KSP 编译速度更快

    // 依赖注入 Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")

    // 桌面小组件 Glance
    implementation("androidx.glance:glance-appwidget:1.0.0")

    // 图表 Vico
    implementation("com.patrykandpatrick.vico:compose-m3:1.14.0")
}
```

**建议：**
先不要急着引入图表和 Markdown，先用 **Room + Compose + AlarmManager** 把最核心的“添加-复习-提醒”闭环跑通。


-----

### 三、 数据库设计 (最关键部分)

这是开发 Memory Helper 类应用最核心的部分。数据库设计得好，后期的**算法实现**、**图表统计**和**数据同步**都会非常轻松。如果设计不好，后面改表结构会非常痛苦。

我们将使用 Android 官方推荐的 **Room Database**。以下是四张核心表的设计，包含字段定义、类型选择以及**设计背后的思考**。

-----

### 1\. 核心表概览 (ER 关系)

  * **`Notebook` (记忆本)**：分类容器（如：考研政治、英语单词）。
      * *1 对 多* $\rightarrow$
  * **`MemoryItem` (记忆条目)**：具体的知识点。
      * *多 对 1* $\rightarrow$
  * **`ReviewCurve` (复习曲线)**：定义复习节奏（如：艾宾浩斯标准版）。
      * *1 对 多* $\rightarrow$
  * **`ReviewLog` (复习日志)**：每次复习的历史记录（用于画统计图）。

-----

### 2\. 详细表结构设计

#### 表一：ReviewCurve (复习曲线策略表)

**作用**：存储“规则”。不同的知识点可能需要不同的复习密度。

```kotlin
@Entity(tableName = "review_curves")
data class ReviewCurve(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    @ColumnInfo(name = "name") 
    val name: String, // 曲线名称，如 "标准艾宾浩斯", "短期高频突击"

    @ColumnInfo(name = "intervals_json") 
    val intervalsJson: String, 
    // 【关键设计】：
    // 数据库不能直接存 List。我们需要用 TypeConverter 把 List<Long> 转成 JSON 字符串存进去。
    // 例如："[5, 30, 720, 1440, 2880, 5760]" (单位：分钟)
    // 对应：5分, 30分, 12小时, 1天, 2天, 4天

    @ColumnInfo(name = "is_default") 
    val isDefault: Boolean = false // 是否是新建条目时默认使用的曲线
)
```

#### 表二：Notebook (记忆本/分类表)

**作用**：用于管理和隔离不同的学习资料。

```kotlin
@Entity(tableName = "notebooks")
data class Notebook(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    val name: String, // 记忆本名字，如 "英语四级"

    val color: Int, // 封面颜色或图标颜色 (存储 Color 的 Int 值)
    
    @ColumnInfo(name = "default_curve_id")
    val defaultCurveId: Long // 该记忆本下的新笔记，默认使用哪条曲线？
)
```

#### 表三：MemoryItem (记忆条目表 - **最核心**)

**作用**：存储实际的学习内容和当前的复习状态。
**优化**：在这个表上我们需要频繁查询“哪些是今天该复习的”，所以需要加**索引 (Index)**。

```kotlin
@Entity(
    tableName = "memory_items",
    // 外键约束：如果删除了 Notebook，里面的 Item 也应该自动删除 (CASCADE)
    foreignKeys = [
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebook_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ReviewCurve::class,
            parentColumns = ["id"],
            childColumns = ["curve_id"],
            onDelete = ForeignKey.RESTRICT // 防止误删正在使用的曲线
        )
    ],
    // 索引：极大加快 "查询今天待复习列表" 的速度
    indices = [Index(value = ["next_review_time"]), Index(value = ["notebook_id"])]
)
data class MemoryItem(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    @ColumnInfo(name = "notebook_id") 
    val notebookId: Long, // 所属记忆本

    @ColumnInfo(name = "curve_id") 
    val curveId: Long, // 绑定的复习曲线

    // --- 内容区 ---
    val title: String, // 标题
    val content: String, // 正文 (支持 Markdown)
    
    @ColumnInfo(name = "image_paths")
    val imagePaths: String, // 存储图片路径的列表 (JSON格式)，不要直接存二进制图片流！

    // --- 状态区 (算法核心) ---
    @ColumnInfo(name = "created_time") 
    val createdTime: Long, // 创建时间

    @ColumnInfo(name = "status")
    val status: Int, 
    // 0: 新建(New), 1: 复习中(Reviewing), 2: 已完成(Completed), 3: 已暂停(Paused)

    @ColumnInfo(name = "stage_index")
    val stageIndex: Int, 
    // 【算法关键】：当前复习到了曲线的第几个阶段？
    // 比如 stageIndex = 2，对应曲线 intervals[2] = 12小时。

    @ColumnInfo(name = "next_review_time")
    val nextReviewTime: Long, 
    // 【查询关键】：下一次复习的绝对时间戳。
    // 我们查询 "今天需要复习什么" 其实就是 select * where nextReviewTime < System.currentTimeMillis()

    @ColumnInfo(name = "last_review_time")
    val lastReviewTime: Long = 0 // 上一次复习的时间 (用于计算是否延期)
)
```

#### 表四：ReviewLog (复习日志/统计表)

**作用**：记录每一次打钩操作，用于生成“遗忘曲线图”和“热力图”。

```kotlin
@Entity(
    tableName = "review_logs",
    indices = [Index(value = ["item_id"]), Index(value = ["actual_review_time"])]
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0,

    @ColumnInfo(name = "item_id") 
    val itemId: Long,

    @ColumnInfo(name = "actual_review_time") 
    val actualReviewTime: Long, // 用户实际点击复习的时间

    @ColumnInfo(name = "planned_review_time") 
    val plannedReviewTime: Long, // 原计划的时间
    
    @ColumnInfo(name = "delay_duration")
    val delayDuration: Long, // 拖延了多久 (actual - planned)，用于分析拖延症

    @ColumnInfo(name = "review_action")
    val reviewAction: Int 
    // 动作类型：1: 记住了(Next), 2: 忘记了(Reset/Retry), 3: 跳过(Skip)
)
```

-----

### 3\. 关键技术细节 (Technical Implementation)

#### A. TypeConverter (类型转换器)

Room 默认只支持基础类型（Int, String, Long 等）。对于 `List<Long>` (曲线间隔) 和 `List<String>` (图片路径)，我们需要自定义转换器。

```kotlin
class Converters {
    // 处理 List<Long> -> String (JSON)
    @TypeConverter
    fun fromListLong(value: List<Long>): String {
        return Gson().toJson(value) // 或者用 Kotlin Serialization
    }

    @TypeConverter
    fun toListLong(value: String): List<Long> {
        val type = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, type)
    }
}
```

#### B. DAO (查询接口) 设计

这是你操作数据库的方法。

```kotlin
@Dao
interface MemoryItemDao {
    // 核心查询：获取所有截止到当前时间需要复习的条目
    // 状态必须是 1 (Reviewing)，且 next_review_time <= 现在
    @Query("""
        SELECT * FROM memory_items 
        WHERE next_review_time <= :currentTime 
        AND status = 1 
        ORDER BY next_review_time ASC
    """)
    fun getItemsDueReview(currentTime: Long): Flow<List<MemoryItem>>

    // 统计查询：获取未来几天的复习量 (用于日历上的小点)
    @Query("""
        SELECT count(*) FROM memory_items 
        WHERE next_review_time BETWEEN :start AND :end
    """)
    fun getReviewCountBetween(start: Long, end: Long): Int
}
```

-----

### 4\. 为什么这么设计？(QA)

  * **Q: 为什么 `nextReviewTime` 不在运行时算，而是存进数据库？**
      * A: 性能。如果你有 1000 个单词，每次打开 App 都要把所有单词拿出来遍历一遍计算“哪个该复习了”，手机会发烫。存在数据库里，直接用 SQL 的 `WHERE time < now` 瞬间就能查出来。
  * **Q: 为什么 `intervals` 存分钟而不是毫秒？**
      * A: 存分钟更灵活，不容易溢出（虽然 Long 很难溢出）。而且分钟对于用户自定义曲线来说，是最直观的最小单位。
  * **Q: 图片为什么只存路径？**
      * A: 数据库存大文件（Blob）会导致读取极慢。图片存手机文件系统，数据库只存类似 `/data/user/0/com.app/files/img_123.jpg` 的字符串索引，这是标准做法。

这套数据库设计已经足以支撑一个**商业级**的记忆 App 了。你只需要在此基础上增加 UI 层的调用即可。
-----

### 四、 核心逻辑实现

这部分是 App 的大脑。有了数据库只是有了“仓库”，现在我们需要编写“物流逻辑”：**如何让货物（记忆条目）在正确的时间点被送达（提醒复习）**。

核心逻辑主要包含四个部分：

1.  **新建条目初始化逻辑**（刚背完怎么算？）
2.  **复习判决逻辑**（记住了/忘了怎么算？）
3.  **智能调度逻辑**（如何设置系统闹钟？）
4.  **状态查询逻辑**（首页显示什么？）

我们将这些逻辑封装在一个 `MemoryRepository` 或 `UseCase` 层中。

-----

### 一、 新建条目初始化逻辑 (Initialization)

当用户在界面上输入完内容，点击“保存”时，系统需要立即计算第一次复习时间。

**场景**：用户刚添加了一个单词，使用的是“标准艾宾浩斯曲线”（第一个间隔是5分钟）。

```kotlin
suspend fun addNewItem(
    title: String, 
    content: String, 
    notebookId: Long, 
    curveId: Long
) {
    // 1. 获取对应的曲线详情
    val curve = curveDao.getCurveById(curveId)
    val firstIntervalMinutes = curve.intervals[0] // 获取第1个阶段，例如 5分钟

    // 2. 计算第一次复习时间 (当前时间 + 5分钟)
    val now = System.currentTimeMillis()
    val firstReviewTime = now + (firstIntervalMinutes * 60 * 1000)

    // 3. 构建实体对象
    val newItem = MemoryItem(
        notebookId = notebookId,
        curveId = curveId,
        title = title,
        content = content,
        createdTime = now,
        status = 1, // 状态设为 "复习中"
        stageIndex = 0, // 当前处于第 0 阶段
        nextReviewTime = firstReviewTime,
        lastReviewTime = now
    )

    // 4. 插入数据库
    val newItemId = memoryItemDao.insert(newItem)

    // 5. 【关键】立即更新系统闹钟
    // 检查这个新时间是否比当前系统里设置的下一个闹钟还要早？如果是，需要更新闹钟。
    alarmScheduler.scheduleNextAlarm()
}
```

-----

### 二、 复习判决逻辑 (Review Action) —— **最核心**

用户点击“记住了”或“忘记了”时，必须更新 `MemoryItem` 并插入 `ReviewLog`。这是一个**事务性操作**（Transaction），必须同时成功或同时失败。

#### 情况 A：用户点击“记住了” (Success)

```kotlin
@Transaction // 保证数据库操作原子性
suspend fun markAsRemembered(item: MemoryItem) {
    val now = System.currentTimeMillis()
    val curve = curveDao.getCurveById(item.curveId) // 获取曲线规则
    
    // 1. 记录日志 (用于统计图表)
    val log = ReviewLog(
        itemId = item.id,
        actualReviewTime = now,
        plannedReviewTime = item.nextReviewTime,
        delayDuration = now - item.nextReviewTime, // 记录拖延了多久
        reviewAction = 1 // 1代表"记住了"
    )
    reviewLogDao.insert(log)

    // 2. 计算下一次时间
    val nextStageIndex = item.stageIndex + 1

    if (nextStageIndex >= curve.intervals.size) {
        // --- 通关逻辑 ---
        // 如果已经到了曲线的尽头 (例如第8个周期背完了)
        val completedItem = item.copy(
            status = 2, // 状态改为 "已完成"
            lastReviewTime = now,
            nextReviewTime = Long.MAX_VALUE // 设为极大概率不再出现在列表中
        )
        memoryItemDao.update(completedItem)
    } else {
        // --- 进阶逻辑 ---
        val nextIntervalMinutes = curve.intervals[nextStageIndex]
        
        // 【核心算法细节】：
        // 下次复习时间 = "现在实际复习的时间" + "间隔"
        // 注意：不要用 item.nextReviewTime + 间隔。
        // 因为如果用户拖延了3天，你应该从今天开始重新计算间隔，否则用户会永远追不上进度。
        val nextTime = now + (nextIntervalMinutes * 60 * 1000)

        val updatedItem = item.copy(
            stageIndex = nextStageIndex,
            nextReviewTime = nextTime,
            lastReviewTime = now,
            status = 1
        )
        memoryItemDao.update(updatedItem)
    }
    
    // 3. 刷新闹钟
    alarmScheduler.scheduleNextAlarm()
}
```

#### 情况 B：用户点击“忘记了” (Failure)

Memory Helper 的逻辑通常是“一旦忘记，重头再来”或者“回退一级”。最严格的是重头再来。

```kotlin
@Transaction
suspend fun markAsForgot(item: MemoryItem) {
    val now = System.currentTimeMillis()
    val curve = curveDao.getCurveById(item.curveId)
    
    // 1. 记录日志
    val log = ReviewLog(
        itemId = item.id,
        actualReviewTime = now,
        plannedReviewTime = item.nextReviewTime,
        delayDuration = 0,
        reviewAction = 2 // 2代表"忘记了"
    )
    reviewLogDao.insert(log)

    // 2. 惩罚逻辑：重置回第 0 阶段
    val firstIntervalMinutes = curve.intervals[0]
    val nextTime = now + (firstIntervalMinutes * 60 * 1000)

    val resetItem = item.copy(
        stageIndex = 0, // 重置为 0
        nextReviewTime = nextTime,
        lastReviewTime = now
    )
    memoryItemDao.update(resetItem)
    
    // 3. 刷新闹钟
    alarmScheduler.scheduleNextAlarm()
}
```

-----

### 三、 智能调度逻辑 (Alarm Scheduler)

你不可能为数据库里的 1000 个单词注册 1000 个系统闹钟（Android 系统有限制，且耗电）。
**核心策略**：永远只设置**离现在最近的那一个**闹钟。

创建一个 `AlarmScheduler` 类：

```kotlin
class AlarmScheduler(private val context: Context, private val dao: MemoryItemDao) {

    suspend fun scheduleNextAlarm() {
        // 1. 查询数据库：谁是下一个需要复习的？
        // 排序取第一个 (LIMIT 1)
        val nextItem = dao.getNearestFutureReviewItem(System.currentTimeMillis())
        
        if (nextItem == null) {
            cancelAlarm() // 后面没有任务了，取消闹钟
            return
        }

        // 2. 设置闹钟
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ITEM_ID", nextItem.id) // 可以在通知里带上具体背什么
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 使用 setExactAndAllowWhileIdle 确保精准
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextItem.nextReviewTime,
            pendingIntent
        )
    }
}
```

**AlarmReceiver (广播接收器)** 的逻辑：
当闹钟响起 -\> 发送通知栏通知 -\> **立即调用 `scheduleNextAlarm()`**。
这样就像接力赛一样，一个响完，自动设置下一个。

-----

### 四、 状态查询逻辑 (UI Logic)

首页通常分为两部分：“待复习”和“未来复习”。

#### 1\. 待复习列表 (Due List)

逻辑：`nextReviewTime <= now`
这里包含了两类：

  * **刚刚到期**：时间刚过。
  * **已逾期**：时间过了很久（例如昨天该背的）。

在 UI 展示时，建议把逾期很久的用**红色高亮**，增加紧迫感。

```kotlin
// ViewModel 中
val dueItems: Flow<List<MemoryItem>> = dao.getItemsDueReview(System.currentTimeMillis())
    .map { list ->
        // 可以在这里按逾期时间长短进行二次排序
        list.sortedBy { it.nextReviewTime }
    }
```

#### 2\. 倒计时刷新 (Live Countdown)

Memory Helper 的列表上通常会显示“剩余 10 分钟”。
由于 `nextReviewTime` 是绝对时间戳，UI 需要动态计算倒计时。

  * **不要在数据库层做倒计时**。
  * **在 UI 层 (Compose)** 做：
    ```kotlin
    @Composable
    fun CountDownText(targetTime: Long) {
        // 每秒刷新一次 UI
        var timeLeft by remember { mutableStateOf(targetTime - System.currentTimeMillis()) }
        
        LaunchedEffect(Unit) {
            while (timeLeft > 0) {
                delay(1000) // 等1秒
                timeLeft = targetTime - System.currentTimeMillis()
            }
        }
        
        Text(text = formatTime(timeLeft))
    }
    ```

-----

### 总结：整个数据的流转过程

1.  **用户添加** -\> 计算出 `10:05` 复习 -\> 写入 DB -\> 设置 `10:05` 的闹钟。
2.  **时间流逝** -\> 到了 `10:05` -\> 闹钟响 -\> 发通知。
3.  **用户点开 App** -\> 看到“待复习列表”里出现了这个条目。
4.  **用户点“记住了”** -\> 算法算出下次是 `10:35` (30分钟后) -\> 更新 DB (stage+1) -\> 重新寻找最近的闹钟并设置。

这套逻辑闭环非常稳健，既保证了数据的准确性，又最大限度地节省了电量。
-----

### 五、 进阶功能与第三方库推荐

要想让你的 App 达到 **Memory Helper** 甚至超越它的水准，仅仅实现“能背”是不够的，还需要“好用”和“好看”。这就需要引入进阶功能和专业的第三方库。

以下是针对现代 Android 开发（Jetpack Compose）环境的详细进阶功能与库推荐清单：

-----

### 一、 富文本与 Markdown 编辑 (让笔记更强大)

**需求场景**：用户不只是背单词，还需要背政治大题、代码片段或数学公式。需要支持**加粗**、*斜体*、`代码块`等格式。

#### 1\. 渲染展示 (Display)

  * **推荐库**：**`com.halilibo.compose-richtext`** 或 **`jeziellago/compose-markdown`**
  * **理由**：
      * 这是目前 Compose 生态下最成熟的 Markdown 渲染库。
      * 它能把 Markdown 字符串直接渲染成原生 UI 组件，而不是由于 WebView 带来的性能损耗。
  * **代码示例**：
    ```kotlin
    // 极其简单，直接渲染 Markdown 文本
    MarkdownText(
        markdown = "这是 **重点内容**，需要复习！\n- 第一点\n- 第二点"
    )
    ```

#### 2\. 编辑输入 (Editing)

  * **挑战**：在移动端实现“所见即所得”的富文本编辑器非常难。
  * **方案**：
      * **轻量级方案**：直接使用原生 `TextField`，让用户输入 Markdown 语法（如手动输入 `**text**`）。
      * **进阶方案**：使用 **`mohamedrejeb/compose-rich-editor`**。
          * 它支持类似 Word 的工具栏（点击 B 按钮自动加粗）。
          * 支持 HTML 和 Markdown 互转。

-----

### 二、 图表统计 (数据可视化)

**需求场景**：

1.  **遗忘曲线图**：展示记忆保留率随时间下降的曲线。
2.  **学习热力图**：像 GitHub 的格子图一样，展示过去一年的努力程度。
3.  **柱状图**：展示每天复习了多少个条目。

#### **推荐库：Vico (强烈推荐)**

  * **Github**: `patrykandpatrick/vico`
  * **理由**：
      * **专为 Compose 设计**：它是轻量级的，不像老牌的 `MPAndroidChart` 那样笨重且依赖 View 系统。
      * **高度可定制**：支持各种线条样式、渐变填充，非常适合画那种“平滑的曲线”。
      * **API 现代**：完全符合 Kotlin DSL 写法。
  * **实现思路**：
      * 从 `ReviewLog` 表中聚合数据 -\> 转为 Vico 的数据模型 (`ChartEntry`) -\> 渲染。

-----

### 三、 桌面小组件 (App Widgets)

**需求场景**：Memory Helper 的核心竞争力之一。用户不需要打开 App，解锁手机就能看到“待复习：5”。

#### **推荐技术：Jetpack Glance**

  * **这是什么**：Google 推出的新框架，让你用写 Compose UI 的方式来写桌面小组件。
  * **以前的痛点**：以前写 Widget 需要写复杂的 XML `RemoteViews`，很难维护，交互处理也很麻烦。
  * **Glance 的优势**：
      * 代码几乎和 App 内部 UI 一模一样。
      * 状态管理更简单（直接读取 DataStore 或 Room）。
  * **核心功能实现**：
      * **ListWidget**：使用 `LazyColumn` 的 Glance 版本，查询数据库显示今日任务。
      * **点击事件**：给列表项绑定 `actionRunCallback<CompleteReviewAction>()`，实现点击即完成复习，无需跳进 App。

-----

### 四、 图片加载 (Image Loading)

**需求场景**：用户拍了一张书本的照片作为笔记内容。

#### **推荐库：Coil**

  * **Github**: `coil-kt/coil`
  * **理由**：
      * **Kotlin First**：基于协程，性能极高。
      * **Compose 完美集成**：使用 `AsyncImage` 组件，一行代码加载图片。
  * **实现细节**：
      * Memory Item 中存储的是图片的本地路径 (`file:///...`)。
      * Coil 会自动处理大图的**下采样 (Downsampling)**，防止加载高清大图导致内存溢出 (OOM)。

-----

### 五、 日历视图 (Calendar View)

**需求场景**：在“复习计划”页面，展示一个月历，日期下面有小圆点（颜色越深代表任务越重）。

#### **推荐库：Kalendar** 或 **Kizitonwose Calendar**

  * **推荐**：**`com.kizitonwose.calendar`** (Compose版本)
  * **理由**：
      * 这是目前安卓界定制性最强的日历库。
      * 它不提供默认 UI，而是给你提供“画板”，你可以完全自己画每一天的格子（比如画成圆角矩形，或者在日期下面画3个不同颜色的点）。
  * **逻辑**：
      * 你需要写一个 SQL 查询：`SELECT date, count(*) FROM items GROUP BY date`。
      * 将查询结果传给 Calendar 库进行渲染。

-----

### 六、 架构与依赖注入 (Architecture)

**需求场景**：随着功能增加，数据库、闹钟管理器、仓库层之间的调用会变得混乱。

#### **推荐库：Hilt** (Google 官方推荐)

  * **作用**：自动管理对象的创建和生命周期。
  * **怎么用**：
      * 你不需要到处写 `db = Room.databaseBuilder(...)`。
      * 只需要在构造函数里加 `@Inject`，Hilt 就会自动把做好的数据库实例塞给你。
      * 这对于**单元测试**和**解耦**至关重要。

-----

### 七、 数据备份与导出 (Backup)

**需求场景**：用户换手机，或者想把数据导出为 Excel/JSON。

#### **推荐库：Kotlin Serialization**

  * **理由**：比 Gson 和 Moshi 更快，且是 Kotlin 官方亲儿子。
  * **实现逻辑**：
    1.  **导出**：查询所有 `MemoryItem` -\> 使用 Serialization 转成 JSON 字符串 -\> 写入 `.json` 文件到手机存储。
    2.  **导入**：读取文件 -\> 解析 JSON -\> 批量插入数据库。
    3.  **CSV 导出**：如果你想支持导出到 Excel，手动拼接逗号分隔符字符串即可，或者使用简单的 CSV 库。

-----

### 总结：你的 `libs.versions.toml` (依赖清单)

如果这是一个真实的项目，你的版本目录文件应该包含这些核心库：

```toml
[libraries]
# 核心架构
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version = "1.12.0" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version = "2.7.0" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version = "1.8.2" }

# UI (Compose)
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version = "2024.02.00" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }

# 数据库 (Room)
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version = "2.6.1" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version = "2.6.1" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version = "2.6.1" }

# 依赖注入 (Hilt)
hilt-android = { group = "com.google.dagger", name = "hilt-android", version = "2.50" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version = "2.50" }

# 图片加载 (Coil)
coil-compose = { group = "io.coil-kt", name = "coil-compose", version = "2.5.0" }

# 桌面小组件 (Glance)
androidx-glance = { group = "androidx.glance", name = "glance-appwidget", version = "1.0.0" }

# 图表 (Vico)
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose-m3", version = "1.14.0" }

# 富文本 (RichText)
compose-richtext = { group = "com.halilibo.compose-richtext", name = "richtext-ui-material3", version = "0.17.0" }

# 日历 (Calendar)
calendar-compose = { group = "com.kizitonwose.calendar", name = "compose", version = "2.5.0" }
```

这些库组合在一起，就是目前 Android 开发的**顶级配置**。你可以放心地基于它们构建你的 Memory Helper 复刻版。
### 六、 开发第一步建议

**不要一上来就画 UI！**

1.  先在 Android Studio 里建立好 `Room` 数据库。
2.  写一个单元测试：创建一个 Item，模拟用户复习，验证 `nextReviewTime` 是否计算正确。
3.  确保算法逻辑跑通了，再用把界面套上去。

