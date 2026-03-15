package com.example.memoryhelper.ui.screens.flashcard

import android.media.MediaPlayer
import android.net.Uri
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.ui.designsystem.PrimaryButton
import com.example.memoryhelper.ui.theme.ErrorCoral
import com.example.memoryhelper.ui.theme.GradientPrimaryEnd
import com.example.memoryhelper.ui.theme.GradientPrimaryStart
import com.example.memoryhelper.ui.theme.PrimaryBlue
import com.example.memoryhelper.ui.theme.SuccessGreen
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Composable
fun FlashcardScreen(
    items: List<MemoryItem>,
    onAgain: (MemoryItem, Long) -> Unit,
    onHard: (MemoryItem, Long) -> Unit,
    onGood: (MemoryItem, Long) -> Unit,
    onEasy: (MemoryItem, Long) -> Unit,
    onComplete: () -> Unit
) {
    val sessionItems = remember { items.toList() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var cardStartedAt by remember(currentIndex) { mutableLongStateOf(System.currentTimeMillis()) }

    if (currentIndex >= sessionItems.size) {
        CompletionScreen(
            totalReviewed = sessionItems.size,
            onDismiss = onComplete
        )
        return
    }

    val currentItem = sessionItems[currentIndex]
    val progress = (currentIndex + 1).toFloat() / sessionItems.size

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
                total = sessionItems.size,
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
                    ActionButtonsGrid(
                        onAgain = {
                            onAgain(currentItem, System.currentTimeMillis() - cardStartedAt)
                            currentIndex++
                            isFlipped = false
                        },
                        onHard = {
                            onHard(currentItem, System.currentTimeMillis() - cardStartedAt)
                            currentIndex++
                            isFlipped = false
                        },
                        onGood = {
                            onGood(currentItem, System.currentTimeMillis() - cardStartedAt)
                            currentIndex++
                            isFlipped = false
                        },
                        onEasy = {
                            onEasy(currentItem, System.currentTimeMillis() - cardStartedAt)
                            currentIndex++
                            isFlipped = false
                        }
                    )
                } else {
                    // Tap hint when not flipped
                    Text(
                        text = "\u70b9\u51fb\u5361\u7247\u67e5\u770b\u7b54\u6848",
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
                text = "\u590d\u4e60 $currentIndex / $total",
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
                    item = item,
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
                    text = "\u9898",
                    style = MaterialTheme.typography.headlineMedium,
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
    item: MemoryItem,
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaRefs = remember(item.mediaRefs, item.imagePaths) { parseMediaRefs(item) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
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
            text = content.ifBlank { "\u65e0\u5185\u5bb9" },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
        )

        if (mediaRefs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            MediaContentSection(
                mediaRefs = mediaRefs,
                onPlayAudio = { ref ->
                    runCatching {
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(context, ref.asUri())
                            setOnCompletionListener {
                                it.release()
                                mediaPlayer = null
                            }
                            prepare()
                            start()
                        }
                    }.onFailure {
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                }
            )
        }
    }
}

@Composable
private fun MediaContentSection(
    mediaRefs: List<FlashcardMediaRef>,
    onPlayAudio: (FlashcardMediaRef) -> Unit
) {
    val imageRefs = mediaRefs.filter { it.type == "image" }
    val audioRefs = mediaRefs.filter { it.type == "audio" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageRefs.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(imageRefs) { ref ->
                    ElevatedCard(
                        modifier = Modifier
                            .width(220.dp)
                            .height(160.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        AsyncImage(
                            model = ref.imageModel(),
                            contentDescription = ref.sourceName ?: "Card image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        if (audioRefs.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                audioRefs.forEachIndexed { index, ref ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        onClick = { onPlayAudio(ref) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ref.sourceName ?: "\u97f3\u9891 ${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "\u70b9\u51fb\u64ad\u653e",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grade buttons shown after reveal.
 */
@Composable
private fun ActionButtonsGrid(
    onAgain: () -> Unit,
    onHard: () -> Unit,
    onGood: () -> Unit,
    onEasy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradeActionButton(
                modifier = Modifier.weight(1f),
                label = "\u91cd\u6765",
                color = ErrorCoral,
                icon = Icons.Default.Close,
                onClick = onAgain
            )
            GradeActionButton(
                modifier = Modifier.weight(1f),
                label = "\u56f0\u96be",
                color = Color(0xFFF6A623),
                icon = Icons.Outlined.Refresh,
                onClick = onHard
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GradeActionButton(
                modifier = Modifier.weight(1f),
                label = "\u8bb0\u4f4f",
                color = SuccessGreen,
                icon = Icons.Default.Check,
                onClick = onGood
            )
            GradeActionButton(
                modifier = Modifier.weight(1f),
                label = "\u8f7b\u677e",
                color = PrimaryBlue,
                icon = Icons.Default.CheckCircle,
                onClick = onEasy
            )
        }
    }
}

@Composable
private fun GradeActionButton(
    modifier: Modifier = Modifier,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = color,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.16f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                }
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (label) {
                        "\u91cd\u6765" -> "\u56de\u5230\u9996\u8f6e"
                        "\u56f0\u96be" -> "\u5c0f\u6b65\u63a8\u8fdb"
                        "\u8bb0\u4f4f" -> "\u6b63\u5e38\u63a8\u8fdb"
                        else -> "\u62c9\u957f\u95f4\u9694"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f)
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
                Surface(
                    shape = CircleShape,
                    color = SuccessGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "本轮复习完成",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "共复习 $totalReviewed 张卡片",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(36.dp))

                PrimaryButton(
                    text = "返回首页",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun parseMediaRefs(item: MemoryItem): List<FlashcardMediaRef> {
    val json = Json { ignoreUnknownKeys = true }

    val structuredRefs = runCatching {
        json.decodeFromString<List<FlashcardMediaRef>>(item.mediaRefs)
            .filter { it.uri.isNotBlank() }
    }.getOrDefault(emptyList())

    if (structuredRefs.isNotEmpty()) {
        return structuredRefs
    }

    return runCatching {
        json.decodeFromString<List<String>>(item.imagePaths)
            .filter { it.isNotBlank() }
            .map { path ->
                FlashcardMediaRef(
                    uri = path,
                    type = "image"
                )
            }
    }.getOrDefault(emptyList())
}

private fun FlashcardMediaRef.imageModel(): Any {
    return if (uri.startsWith("content://")) Uri.parse(uri) else File(uri)
}

private fun FlashcardMediaRef.asUri(): Uri {
    return if (uri.startsWith("content://")) Uri.parse(uri) else Uri.fromFile(File(uri))
}

@Serializable
private data class FlashcardMediaRef(
    val uri: String,
    val type: String,
    val durationMs: Long = 0L,
    val sourceName: String? = null
)

