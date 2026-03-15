package com.example.memoryhelper.ui.screens.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memoryhelper.data.local.dao.DailyPlanRow
import com.example.memoryhelper.data.local.entity.DailyPlanPriority
import com.example.memoryhelper.data.local.entity.ExamPlan
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.data.local.entity.Notebook
import com.example.memoryhelper.ui.designsystem.AppCard
import com.example.memoryhelper.ui.designsystem.AppCardTone
import com.example.memoryhelper.ui.designsystem.AppSpacing
import com.example.memoryhelper.ui.designsystem.AppTextField
import com.example.memoryhelper.ui.designsystem.AppTopBar
import com.example.memoryhelper.ui.designsystem.PrimaryButton
import com.example.memoryhelper.ui.designsystem.SecondaryButton
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Composable
fun ExamScreen(
    viewModel: ExamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activePlan = uiState.activePlan
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreatePlanDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { latestMessage ->
            snackbarHostState.showSnackbar(latestMessage)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text("Exam Plan") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            item {
                if (activePlan == null) {
                    EmptyPlanCard(onCreate = { showCreatePlanDialog = true })
                } else {
                    PlanSummaryCard(
                        plan = activePlan,
                        subjects = uiState.subjects,
                        planCount = uiState.todayPlan.size,
                        generatedCount = uiState.lastGeneratedCount,
                        onAddSubject = { showAddSubjectDialog = true },
                        onGeneratePlan = viewModel::regenerateTodayPlan
                    )
                }
            }

            if (activePlan == null) {
                item {
                    PlanSetupGuideCard()
                }
            }

            if (uiState.subjects.isNotEmpty()) {
                item {
                    SubjectSection(subjects = uiState.subjects, notebooks = uiState.notebooks)
                }
            }

            item {
                TodayPlanSection(rows = uiState.todayPlan)
            }
        }
    }

    if (showCreatePlanDialog) {
        CreatePlanDialog(
            onDismiss = { showCreatePlanDialog = false },
            onCreate = { name, examDate, dailyBudget, overdueLimit ->
                viewModel.createPlan(name, examDate, dailyBudget, overdueLimit)
                showCreatePlanDialog = false
            }
        )
    }

    if (showAddSubjectDialog && activePlan != null) {
        AddSubjectDialog(
            notebooks = uiState.notebooks,
            onDismiss = { showAddSubjectDialog = false },
            onAdd = { name, weight, notebookId ->
                viewModel.addSubject(
                    planId = activePlan.id,
                    name = name,
                    weight = weight,
                    notebookId = notebookId
                )
                showAddSubjectDialog = false
            }
        )
    }
}

@Composable
private fun EmptyPlanCard(
    onCreate: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Accent
    ) {
        Text(
            text = "Exam workspace",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "No study plan yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "Create an exam target first. We will use it to balance overdue work, subject weight, and daily capacity.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PlanMetricChip(
                modifier = Modifier.weight(1f),
                label = "Exam date",
                value = "Set target"
            )
            PlanMetricChip(
                modifier = Modifier.weight(1f),
                label = "Budget",
                value = "Daily load"
            )
            PlanMetricChip(
                modifier = Modifier.weight(1f),
                label = "Subjects",
                value = "Weighted"
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.md))
        PrimaryButton(
            text = "Create Plan",
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PlanSetupGuideCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "How this screen works",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = "The planning flow is short by design: define the target, bind subjects to notebooks, then generate today's queue.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            SetupStep(
                index = "01",
                title = "Create the target exam",
                supporting = "Set the exam date, your base daily budget, and how much overdue debt can spill into today."
            )
            SetupStep(
                index = "02",
                title = "Add weighted subjects",
                supporting = "Tie each subject to a notebook so planning can distribute work by importance and backlog."
            )
            SetupStep(
                index = "03",
                title = "Generate today's plan",
                supporting = "Must-do items are filled first, then remaining capacity is used for recommended work."
            )
        }
    }
}

@Composable
private fun PlanSummaryCard(
    plan: ExamPlan,
    subjects: List<ExamSubject>,
    planCount: Int,
    generatedCount: Int?,
    onAddSubject: () -> Unit,
    onGeneratePlan: () -> Unit
) {
    val daysLeft = (plan.examDateEpochDay - LocalDate.now().toEpochDay()).toInt().coerceAtLeast(0)

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Accent
    ) {
        Text(
            text = "Active plan",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = plan.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = "Exam date ${LocalDate.ofEpochDay(plan.examDateEpochDay)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PlanMetricChip(
                modifier = Modifier.weight(1f),
                label = "Days left",
                value = "$daysLeft"
            )
            PlanMetricChip(
                modifier = Modifier.weight(1f),
                label = "Subjects",
                value = "${subjects.size}"
            )
            PlanMetricChip(
                modifier = Modifier.weight(1f),
                label = "Today",
                value = "$planCount"
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "Daily budget ${plan.dailyBudget}  |  Overdue cap ${plan.overdueCompensationLimit}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        generatedCount?.let {
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = "Last generation $it items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SecondaryButton(
                text = "Add Subject",
                onClick = onAddSubject,
                modifier = Modifier.weight(1f)
            )
            PrimaryButton(
                text = "Generate Today",
                onClick = onGeneratePlan,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SubjectSection(
    subjects: List<ExamSubject>,
    notebooks: List<Notebook>
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Subjects",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = "Weight each subject against a notebook so the generated plan can spread work intentionally.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            subjects.forEach { subject ->
                val notebookName = notebooks.firstOrNull { it.id == subject.notebookId }?.name ?: "Unassigned"
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    tone = AppCardTone.Elevated,
                    padding = PaddingValues(AppSpacing.sm)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(subject.name, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(AppSpacing.xxs))
                            Text(
                                text = "Notebook: $notebookName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PlanMiniPill(label = "W ${subject.weight}")
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayPlanSection(
    rows: List<DailyPlanRow>
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Today Plan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = "Must-do work includes overdue debt. Recommended work fills remaining capacity by subject weight.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        if (rows.isEmpty()) {
            Text(
                text = "No plan generated for today.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                rows.forEach { row ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = if (row.priority == DailyPlanPriority.MUST_DO) {
                            AppCardTone.Accent
                        } else {
                            AppCardTone.Elevated
                        },
                        padding = PaddingValues(AppSpacing.sm)
                    ) {
                        Text(
                            text = row.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = buildString {
                                    append(priorityLabel(row.priority))
                                    row.subjectName?.let { subjectName ->
                                        append(" | ")
                                        append(subjectName)
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                            PlanMiniPill(label = if (row.status == 1) "Done" else "Open")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        tone = AppCardTone.Elevated,
        padding = PaddingValues(AppSpacing.sm)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlanMiniPill(
    label: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SetupStep(
    index: String,
    title: String,
    supporting: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = index,
                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun priorityLabel(priority: Int): String {
    return when (priority) {
        DailyPlanPriority.MUST_DO -> "Must do"
        DailyPlanPriority.RECOMMENDED -> "Recommended"
        else -> "Optional"
    }
}

@Composable
private fun CreatePlanDialog(
    onDismiss: () -> Unit,
    onCreate: (String, LocalDate, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var examDateText by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var dailyBudgetText by remember { mutableStateOf("30") }
    var overdueLimitText by remember { mutableStateOf("15") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Exam Plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Plan name",
                    placeholder = "Example: Civil Service 2026"
                )
                AppTextField(
                    value = examDateText,
                    onValueChange = { examDateText = it },
                    label = "Exam date",
                    placeholder = "YYYY-MM-DD",
                    singleLine = true
                )
                AppTextField(
                    value = dailyBudgetText,
                    onValueChange = { dailyBudgetText = it },
                    label = "Daily budget",
                    singleLine = true
                )
                AppTextField(
                    value = overdueLimitText,
                    onValueChange = { overdueLimitText = it },
                    label = "Overdue cap",
                    singleLine = true
                )
                errorMessage?.let { latestError ->
                    Text(
                        text = latestError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Create",
                onClick = {
                    try {
                        val examDate = LocalDate.parse(examDateText.trim())
                        val dailyBudget = dailyBudgetText.trim().toInt()
                        val overdueLimit = overdueLimitText.trim().toInt()
                        if (name.isBlank()) {
                            errorMessage = "Plan name is required"
                        } else {
                            onCreate(name.trim(), examDate, dailyBudget, overdueLimit)
                        }
                    } catch (_: DateTimeParseException) {
                        errorMessage = "Use YYYY-MM-DD for the exam date"
                    } catch (_: NumberFormatException) {
                        errorMessage = "Budget fields must be integers"
                    }
                }
            )
        },
        dismissButton = {
            SecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun AddSubjectDialog(
    notebooks: List<Notebook>,
    onDismiss: () -> Unit,
    onAdd: (String, Float, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("1.0") }
    var selectedNotebookId by remember { mutableLongStateOf(notebooks.firstOrNull()?.id ?: 0L) }
    var bindNotebook by remember { mutableStateOf(notebooks.isNotEmpty()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Subject") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Subject name",
                    placeholder = "Example: Civil Law"
                )
                AppTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = "Weight",
                    singleLine = true
                )
                if (notebooks.isNotEmpty()) {
                    Text(
                        text = "Bind notebook",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        item {
                            FilterChip(
                                selected = !bindNotebook,
                                onClick = { bindNotebook = false },
                                label = { Text("No binding") }
                            )
                        }
                        items(notebooks) { notebook ->
                            FilterChip(
                                selected = bindNotebook && selectedNotebookId == notebook.id,
                                onClick = {
                                    bindNotebook = true
                                    selectedNotebookId = notebook.id
                                },
                                label = { Text(notebook.name) }
                            )
                        }
                    }
                }
                errorMessage?.let { latestError ->
                    Text(
                        text = latestError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Add",
                onClick = {
                    val weight = weightText.trim().toFloatOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Subject name is required"
                    } else if (weight == null || weight <= 0f) {
                        errorMessage = "Weight must be greater than 0"
                    } else {
                        onAdd(name.trim(), weight, if (bindNotebook) selectedNotebookId else null)
                    }
                }
            )
        },
        dismissButton = {
            SecondaryButton(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}
