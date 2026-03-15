package com.example.memoryhelper.ui.designsystem

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppCardTone {
    Surface,
    Elevated,
    Accent
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    tone: AppCardTone = AppCardTone.Surface,
    padding: PaddingValues = PaddingValues(AppSpacing.md),
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val animatedElevation by animateDpAsState(
        targetValue = when (tone) {
            AppCardTone.Surface -> elevation / 2
            AppCardTone.Elevated -> elevation
            AppCardTone.Accent -> elevation + 2.dp
        },
        label = "cardElevation"
    )
    val containerColor = when (tone) {
        AppCardTone.Surface -> MaterialTheme.colorScheme.surface
        AppCardTone.Elevated -> MaterialTheme.colorScheme.surfaceVariant
        AppCardTone.Accent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f)
    }
    val borderColor = when (tone) {
        AppCardTone.Surface -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
        AppCardTone.Elevated -> Color.Transparent
        AppCardTone.Accent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    }
    val cardColors = CardDefaults.cardColors(containerColor = containerColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            colors = cardColors,
            elevation = cardElevation,
            border = BorderStroke(1.dp, borderColor),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            colors = cardColors,
            elevation = cardElevation,
            border = BorderStroke(1.dp, borderColor),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}
