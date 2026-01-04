# Project Instructions: Memory Helper Android App

## 1. Project Context & Goal
We are building a **Spaced Repetition System (SRS)** Android application inspired by "Memory Helper".
**Core Logic:** The app schedules review notifications based on the Ebbinghaus Forgetting Curve.
**Primary Goal:** To assist users in remembering content (text, images) by notifying them at specific intervals (e.g., 5 min, 30 min, 12h, 1 day...).

---

## 2. Technology Stack (Strict Adherence)
**You must strictly use the following Modern Android Development (MAD) stack:**

* **Language:** Kotlin (Latest version).
* **UI Framework:** **Jetpack Compose** (Material 3). **NO XML Layouts.**
* **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
* **Dependency Injection:** **Hilt**.
* **Database:** **Room Database** (SQLite).
* **Async Processing:** Kotlin Coroutines & Flow.
* **Navigation:** Navigation Compose.
* **Scheduling:**
    * **AlarmManager (`setExactAndAllowWhileIdle`)**: For precise review notifications. **DO NOT use WorkManager for immediate/precise reminders.**
    * **WorkManager**: Only for maintenance tasks (cleanup, backup) or daily stats generation.
* **Third-Party Libraries:**
    * **Charts:** `com.patrykandpatrick.vico` (Vico) - For Ebbinghaus curves.
    * **Rich Text:** `com.halilibo.compose-richtext` or `jeziellago/compose-markdown`.
    * **Image Loading:** `io.coil-kt:coil-compose`.
    * **Widgets:** Jetpack Glance.
    * **Serialization:** Kotlin Serialization (JSON).

---

## 3. Database Schema (Room)
The database is the core. Implement the following entities strictly.

### 3.1 `ReviewCurve` (Strategies)
* Stores custom review intervals.
* **Fields:** `id` (PK), `name` (String), `intervals_json` (String, stores `List<Long>` in minutes via TypeConverter), `is_default` (Boolean).

### 3.2 `Notebook` (Categories)
* Organizes items.
* **Fields:** `id` (PK), `name` (String), `color` (Int), `default_curve_id` (FK).

### 3.3 `MemoryItem` (The Content)
* **Fields:**
    * `id` (PK)
    * `notebook_id` (FK), `curve_id` (FK)
    * `title` (String), `content` (String, Markdown support)
    * `image_paths` (String, JSON List of local file paths)
    * `status` (Int: 0=New, 1=Reviewing, 2=Completed, 3=Paused)
    * `stage_index` (Int: Current index in the curve interval list)
    * `next_review_time` (Long: Timestamp for NEXT alarm)
    * `last_review_time` (Long)
* **Indices:** Index on `next_review_time` (Crucial for querying due items).

### 3.4 `ReviewLog` (Stats)
* **Fields:** `id` (PK), `item_id` (FK), `actual_review_time` (Long), `planned_review_time` (Long), `review_action` (Int: 1=Remembered, 2=Forgot).

---

## 4. Core Algorithms (Business Logic)

### 4.1 Calculating Next Review
When user clicks "Remember":
1.  Check `stage_index`. If last stage -> Mark status as `Completed`.
2.  Else -> `next_review_time` = `System.currentTimeMillis()` + `curve.intervals[stage_index + 1]`.
3.  Increment `stage_index`.

When user clicks "Forgot":
1.  Reset `stage_index` to 0.
2.  `next_review_time` = `System.currentTimeMillis()` + `curve.intervals[0]`.

### 4.2 Alarm Scheduling Logic
* **Strategy:** Only schedule **ONE** system alarm at a time (the nearest future `next_review_time`).
* **Flow:**
    1.  User updates an item (Add/Review).
    2.  Query DB: `SELECT min(next_review_time) FROM items WHERE status=1 AND next_review_time > now`.
    3.  `AlarmManager.setExactAndAllowWhileIdle(time, pendingIntent)`.
    4.  When Alarm fires -> Show Notification -> Repeat Query to set the *next* alarm.

---

## 5. Development Workflow with Claude Code

### 5.1 Tools & MCP Usage
* **Dependencies:** When adding a new library, use **Brave Search** to find the latest version number and implementation guide (e.g., "latest version of Vico charts compose").
* **Database Debugging:**
    1.  Use Terminal: `adb pull /data/data/com.your.package/databases/app_db.db ./debug.db`
    2.  Use **SQLite MCP**: Analyze `debug.db` to verify if `next_review_time` is calculated correctly.
* **Compilation:**
    * Use Terminal: `./gradlew assembleDebug` to verify builds.
    * If build fails, analyze the output and fix immediately.

### 5.2 Coding Standards
* **File Structure:**
    * `data/local/entity`: Room Entities.
    * `data/local/dao`: DAOs.
    * `data/repository`: Logic for algorithms and DB access.
    * `ui/screens`: Composable screens.
    * `ui/components`: Reusable UI parts.
    * `di`: Hilt Modules.
* **State Management:** Use `ViewModel` holding `StateFlow` or `MutableState`. Expose immutable `State` to UI.

---

## 6. Implementation Phases (Roadmap)

1.  **Phase 1 (Skeleton):** Setup Room DB, Entities, Hilt, and Basic "Add Item" UI.
2.  **Phase 2 (Logic):** Implement the `MemoryRepository` with the Next Review Algorithm.
3.  **Phase 3 (Trigger):** Implement `AlarmManager` and Notifications.
4.  **Phase 4 (UI Polish):** List View with "Due Now" logic, Rich Text display.
5.  **Phase 5 (Advanced):** Vico Charts, Widgets, Backup.

## 7. Important Constraints
* **Never** block the UI thread. All DB operations must be `suspend` functions using `Dispatchers.IO`.
* **Never** put countdown logic (Timer) in the Database or ViewModel. Countdowns must be handled in the UI layer (Compose `LaunchedEffect`).
* Handle Permission: `POST_NOTIFICATIONS` (Android 13+) and `SCHEDULE_EXACT_ALARM` (Android 12+).