package com.example.memoryhelper.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memoryhelper.data.repository.ActivePlanProgress
import com.example.memoryhelper.domain.scheduler.ReviewGradeOption
import com.example.memoryhelper.ui.designsystem.AppCard
import com.example.memoryhelper.ui.designsystem.AppCardTone
import com.example.memoryhelper.ui.designsystem.AppSpacing
import com.example.memoryhelper.ui.designsystem.AppTopBar
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text("Stats") }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                ReviewPulseHero(uiState = uiState)
                SignalDeck(uiState = uiState)
                uiState.activePlanProgress?.let { progress ->
                    PlanMomentumCard(progress = progress)
                }
                ReviewVolumeCard(uiState = uiState)
                GradeDistributionCard(
                    gradeBreakdown = uiState.gradeBreakdown,
                    totalReviews = uiState.totalReviews
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
            }
        }
    }
}

@Composable
private fun ReviewPulseHero(
    uiState: StatsUiState
) {
    val dailyPace = if (uiState.chartData.isEmpty()) 0 else uiState.totalReviews.toFloat() / uiState.chartData.size.toFloat()

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Accent
    ) {
        Text(
            text = "Review pulse",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "${uiState.totalReviews}",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = "review actions captured over the last seven days",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            PulseMetricChip(
                modifier = Modifier.weight(1f),
                label = "Daily pace",
                value = String.format(Locale.getDefault(), "%.1f/day", dailyPace)
            )
            PulseMetricChip(
                modifier = Modifier.weight(1f),
                label = "Overdue",
                value = formatPercent(uiState.overdueRate)
            )
            PulseMetricChip(
                modifier = Modifier.weight(1f),
                label = "Response",
                value = formatResponseTime(uiState.averageResponseMs)
            )
        }
    }
}

@Composable
private fun PulseMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SignalDeck(
    uiState: StatsUiState
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SignalCard(
                modifier = Modifier.weight(1f),
                title = "Overdue reviews",
                value = uiState.overdueReviews.toString(),
                supporting = "Items answered after their due time"
            )
            SignalCard(
                modifier = Modifier.weight(1f),
                title = "Average response",
                value = formatResponseTime(uiState.averageResponseMs),
                supporting = "Time spent before grading"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            SignalCard(
                modifier = Modifier.weight(1f),
                title = "Strong recalls",
                value = strongRecallCount(uiState).toString(),
                supporting = "Good + Easy responses this week"
            )
            SignalCard(
                modifier = Modifier.weight(1f),
                title = "Recovery load",
                value = weakRecallCount(uiState).toString(),
                supporting = "Again + Hard responses this week"
            )
        }
    }
}

@Composable
private fun SignalCard(
    title: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        tone = AppCardTone.Surface
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlanMomentumCard(
    progress: ActivePlanProgress
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Elevated
    ) {
        Text(
            text = "Plan momentum",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = progress.planName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${progress.doneCount}/${progress.totalCount} done today",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatPercent(progress.completionRate),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        LinearProgressIndicator(
            progress = { progress.completionRate.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReviewVolumeCard(
    uiState: StatsUiState
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(336.dp),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Seven-day volume",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = "Use this to check whether the plan is producing a stable review rhythm instead of sudden debt spikes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))

        if (uiState.totalReviews > 0) {
            val dataPoints = uiState.chartData.associate { it.date to it.count }
            ReviewChart(dataPoints = dataPoints)
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No review data yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GradeDistributionCard(
    gradeBreakdown: List<GradeBreakdownUi>,
    totalReviews: Int
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Recall distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxs))
        Text(
            text = "This spread shows how often you are recovering forgotten material versus reinforcing stable memory.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))

        if (totalReviews == 0) {
            Text(
                text = "No grading signals yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                gradeBreakdown.forEach { row ->
                    val ratio = if (totalReviews == 0) 0f else row.count.toFloat() / totalReviews.toFloat()
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = AppCardTone.Elevated,
                        padding = androidx.compose.foundation.layout.PaddingValues(AppSpacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = gradeLabel(row.grade),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                                Text(
                                    text = distributionSupport(row.grade),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${row.count}  ${formatPercent(ratio)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        LinearProgressIndicator(
                            progress = { ratio.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewChart(
    dataPoints: Map<String, Int>
) {
    val modelProducer = CartesianChartModelProducer.build()

    LaunchedEffect(dataPoints) {
        modelProducer.tryRunTransaction {
            columnSeries {
                series(dataPoints.values.toList())
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxSize()
    )
}

private fun formatPercent(value: Float): String {
    return String.format(Locale.getDefault(), "%.0f%%", value * 100f)
}

private fun formatResponseTime(responseMs: Long): String {
    if (responseMs <= 0L) return "n/a"
    return if (responseMs < 1_000L) {
        "${responseMs}ms"
    } else {
        String.format(Locale.getDefault(), "%.1fs", responseMs / 1_000f)
    }
}

private fun gradeLabel(grade: ReviewGradeOption): String {
    return when (grade) {
        ReviewGradeOption.AGAIN -> "Again"
        ReviewGradeOption.HARD -> "Hard"
        ReviewGradeOption.GOOD -> "Good"
        ReviewGradeOption.EASY -> "Easy"
    }
}

private fun distributionSupport(grade: ReviewGradeOption): String {
    return when (grade) {
        ReviewGradeOption.AGAIN -> "Memory collapsed and the item returned to recovery."
        ReviewGradeOption.HARD -> "The answer was possible, but still unstable."
        ReviewGradeOption.GOOD -> "Normal successful recall for the current stage."
        ReviewGradeOption.EASY -> "Strong recall with room to stretch the interval."
    }
}

private fun strongRecallCount(uiState: StatsUiState): Int {
    return uiState.gradeBreakdown
        .filter { it.grade == ReviewGradeOption.GOOD || it.grade == ReviewGradeOption.EASY }
        .sumOf { it.count }
}

private fun weakRecallCount(uiState: StatsUiState): Int {
    return uiState.gradeBreakdown
        .filter { it.grade == ReviewGradeOption.AGAIN || it.grade == ReviewGradeOption.HARD }
        .sumOf { it.count }
}
