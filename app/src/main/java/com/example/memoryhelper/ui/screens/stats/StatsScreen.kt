package com.example.memoryhelper.ui.screens.stats

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memoryhelper.R
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
                title = { Text(stringResource(R.string.review_statistics)) }
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
            AnimatedVisibility(visible = !uiState.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(AppSpacing.lg)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        tone = AppCardTone.Accent
                    ) {
                        Text(
                            text = stringResource(R.string.last_7_days),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        Text(
                            text = stringResource(R.string.reviews_count, uiState.totalReviews),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    MetricsOverviewCard(uiState = uiState)

                    uiState.activePlanProgress?.let { progress ->
                        PlanCompletionCard(progress = progress)
                    }

                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        tone = AppCardTone.Surface
                    ) {
                        Text(
                            text = stringResource(R.string.daily_reviews),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = AppSpacing.md)
                        )

                        if (uiState.totalReviews > 0) {
                            val dataPoints = uiState.chartData.associate { it.date to it.count }
                            ReviewChart(dataPoints = dataPoints)
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_reviews_yet),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    GradeDistributionCard(
                        gradeBreakdown = uiState.gradeBreakdown,
                        totalReviews = uiState.totalReviews
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCompletionCard(
    progress: com.example.memoryhelper.data.repository.ActivePlanProgress
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Today Plan Completion",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = progress.planName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${progress.doneCount}/${progress.totalCount} completed",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatPercent(progress.completionRate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
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
private fun MetricsOverviewCard(
    uiState: StatsUiState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Study Signals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            MetricCell(
                modifier = Modifier.weight(1f),
                label = "Overdue rate",
                value = formatPercent(uiState.overdueRate),
                supporting = "${uiState.overdueReviews} overdue"
            )
            MetricCell(
                modifier = Modifier.weight(1f),
                label = "Avg response",
                value = formatResponseTime(uiState.averageResponseMs),
                supporting = "Per review action"
            )
        }
    }
}

@Composable
private fun MetricCell(
    label: String,
    value: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        tone = AppCardTone.Elevated
    ) {
        Text(
            text = label,
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
private fun GradeDistributionCard(
    gradeBreakdown: List<GradeBreakdownUi>,
    totalReviews: Int
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Surface
    ) {
        Text(
            text = "Grade Distribution",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))

        if (totalReviews == 0) {
            Text(
                text = "No review signals yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                gradeBreakdown.forEach { row ->
                    val ratio = if (totalReviews == 0) 0f else row.count.toFloat() / totalReviews.toFloat()
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = gradeLabel(row.grade),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${row.count} (${formatPercent(ratio)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
