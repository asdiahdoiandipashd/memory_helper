package com.example.memoryhelper.ui.screens.flashcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.memoryhelper.R
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.ui.theme.ErrorCoral
import com.example.memoryhelper.ui.theme.GradientPrimaryEnd
import com.example.memoryhelper.ui.theme.GradientPrimaryStart
import com.example.memoryhelper.ui.theme.PrimaryBlue
import com.example.memoryhelper.ui.theme.SuccessGreen

@Composable
fun FlashcardScreen(
    items: List<MemoryItem>,
    onRemember: (MemoryItem) -> Unit,
    onForgot: (MemoryItem) -> Unit,
    onComplete: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    if (currentIndex >= items.size) {
        CompletionScreen(
            totalReviewed = items.size,
            onDismiss = onComplete
        )
        return
    }

    val currentItem = items[currentIndex]
    val progress = (currentIndex + 1).toFloat() / items.size

    // Immersive gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GradientPrimaryStart,
                        GradientPrimaryStart.copy(alpha = 0.7f),
                        GradientPrimaryEnd
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Section - Top
            ProgressHeader(
                currentIndex = currentIndex + 1,
                total = items.size,
                progress = progress
            )

            Spacer(modifier = Modifier.weight(0.8f))

            // Flashcard with 3D Flip Animation
            FlashcardView(
                item = currentItem,
                isFlipped = isFlipped,
                onFlip = { isFlipped = !isFlipped }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Control Buttons - Bottom
            AnimatedContent(
                targetState = isFlipped,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f)) togetherWith
                            (fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.8f))
                },
                label = "buttons"
            ) { flipped ->
                if (flipped) {
                    ActionButtonsRow(
                        onForgot = {
                            onForgot(currentItem)
                            currentIndex++
                            isFlipped = false
                        },
                        onRemember = {
                            onRemember(currentItem)
                            currentIndex++
                            isFlipped = false
                        }
                    )
                } else {
                    // Tap hint when not flipped
                    Text(
                        text = stringResource(R.string.tap_to_flip),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Progress header with modern styling.
 */
@Composable
private fun ProgressHeader(
    currentIndex: Int,
    total: Int,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.flashcard_progress, currentIndex, total),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Percentage badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar with rounded ends
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f),
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Flashcard with 3D flip animation.
 */
@Composable
private fun FlashcardView(
    item: MemoryItem,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "rotation"
    )

    // Scale animation for depth effect
    val scale by animateFloatAsState(
        targetValue = if (rotation in 70f..110f) 0.92f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
                scaleX = scale
                scaleY = scale
            },
        onClick = onFlip,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 16.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Content changes based on flip state
            if (rotation <= 90f) {
                // Front: Title (Question)
                FrontContent(title = item.title)
            } else {
                // Back: Content (Answer) - mirrored to appear correctly
                BackContent(
                    content = item.content,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}

/**
 * Front side of the card - Question/Title.
 */
@Composable
private fun FrontContent(title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Question mark icon
        Surface(
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.1f),
            modifier = Modifier.size(64.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Back side of the card - Answer/Content.
 */
@Composable
private fun BackContent(
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Answer indicator
        Surface(
            shape = CircleShape,
            color = SuccessGreen.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = content.ifBlank { stringResource(R.string.no_content) },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
        )
    }
}

/**
 * Action buttons row - Large circular buttons.
 */
@Composable
private fun ActionButtonsRow(
    onForgot: () -> Unit,
    onRemember: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Forgot button - Large Red Circle
        Surface(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    clip = false
                ),
            shape = CircleShape,
            color = ErrorCoral,
            onClick = onForgot
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.forgot),
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(48.dp))

        // Remember button - Large Green Circle
        Surface(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    clip = false
                ),
            shape = CircleShape,
            color = SuccessGreen,
            onClick = onRemember
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.remember),
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Completion screen shown after all items reviewed.
 */
@Composable
private fun CompletionScreen(
    totalReviewed: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SuccessGreen.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration emoji with background
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF9C4),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "🎉",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(R.string.review_complete),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.reviewed_items, totalReviewed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text(
                        text = stringResource(R.string.back_to_home),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
