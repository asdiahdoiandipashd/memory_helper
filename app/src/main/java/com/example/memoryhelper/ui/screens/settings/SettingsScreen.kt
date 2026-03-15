package com.example.memoryhelper.ui.screens.settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.memoryhelper.MemoryHelperApplication
import com.example.memoryhelper.ui.designsystem.AppCard
import com.example.memoryhelper.ui.designsystem.AppCardTone
import com.example.memoryhelper.ui.designsystem.AppSpacing
import com.example.memoryhelper.ui.designsystem.AppTopBar
import com.example.memoryhelper.ui.designsystem.PrimaryButton
import com.example.memoryhelper.ui.designsystem.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshAllPermissions()
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshAllPermissions()
        }
    }

    LaunchedEffect(uiState.isTestNotificationSent) {
        if (uiState.isTestNotificationSent) {
            snackbarHostState.showSnackbar("Test notification sent.")
            viewModel.resetTestNotificationFlag()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = { Text("Settings") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            ReliabilityHeroCard(uiState = uiState)

            SectionTitle(
                title = "Reliability controls",
                supporting = "These switches decide whether review reminders can survive modern Android battery and alarm policies."
            )

            PermissionItem(
                title = "Notifications",
                description = "Allow the app to surface due review reminders and result feedback.",
                isGranted = uiState.notificationPermission.isGranted,
                onRequestPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openAppNotificationSettings(context)
                    }
                }
            )

            PermissionItem(
                title = "Exact alarms",
                description = "Required for precise reminder timing on Android 12 and above.",
                isGranted = uiState.exactAlarmPermission.isGranted,
                isRequired = uiState.exactAlarmPermission.isRequired,
                onRequestPermission = { openExactAlarmSettings(context) }
            )

            PermissionItem(
                title = "Battery protection whitelist",
                description = "Prevents the system from suspending background reminder delivery. This is the most critical control.",
                isGranted = uiState.batteryOptimizationWhitelisted.isGranted,
                isCritical = true,
                buttonText = if (uiState.batteryOptimizationWhitelisted.isGranted) "Protected" else "Protect app",
                onRequestPermission = { requestBatteryOptimizationWhitelist(context) }
            )

            SectionTitle(
                title = "Diagnostics",
                supporting = "Use this before blaming the scheduler. It verifies that the notification channel still reaches the device."
            )

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                tone = AppCardTone.Surface
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Send a live test notification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.xxs))
                        Text(
                            text = "If this fails, reminder delivery is blocked at the device or channel level, not inside the review flow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                PrimaryButton(
                    text = "Send test notification",
                    onClick = {
                        sendTestNotification(context)
                        viewModel.onTestNotificationSent()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            BackupRestoreSection(viewModel = viewModel)

            SectionTitle(
                title = "Keep it stable",
                supporting = "These are the device-level habits that matter most when reminders feel unreliable."
            )

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                tone = AppCardTone.Elevated
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    TipItem(text = "On Xiaomi, Huawei, OPPO and similar OEM skins, manually allow background activity for Memory Helper.")
                    TipItem(text = "If reminders still drift, disable aggressive battery saver rules for this app.")
                    TipItem(text = "Check that notification sound, vibration, and channel alerts were not muted by the system.")
                    TipItem(text = "Export JSON backups before large imports or device migration.")
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))
        }
    }
}

@Composable
private fun ReliabilityHeroCard(
    uiState: SettingsUiState
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Accent
    ) {
        Text(
            text = "Reminder cockpit",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "Protect the delivery path",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "A perfect SRS schedule still fails if Android suppresses alerts. Keep these three controls green before judging review timing.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            HeroStatusChip(
                modifier = Modifier.weight(1f),
                label = "Alerts",
                ready = uiState.notificationPermission.isGranted
            )
            HeroStatusChip(
                modifier = Modifier.weight(1f),
                label = "Alarm",
                ready = uiState.exactAlarmPermission.isGranted || !uiState.exactAlarmPermission.isRequired
            )
            HeroStatusChip(
                modifier = Modifier.weight(1f),
                label = "Battery",
                ready = uiState.batteryOptimizationWhitelisted.isGranted
            )
        }
    }
}

@Composable
private fun HeroStatusChip(
    label: String,
    ready: Boolean,
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
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
            )
            Text(
                text = if (ready) "Ready" else "Check",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    supporting: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = supporting,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    isRequired: Boolean = true,
    isCritical: Boolean = false,
    buttonText: String = if (isGranted) "Granted" else "Review setting",
    onRequestPermission: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = if (isCritical && !isGranted) AppCardTone.Accent else AppCardTone.Surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                contentDescription = null,
                tint = when {
                    isGranted -> MaterialTheme.colorScheme.tertiary
                    isCritical -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isCritical && !isGranted) {
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Critical",
                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))

                when {
                    !isRequired -> {
                        Text(
                            text = "This Android version does not require the setting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    isGranted -> {
                        SecondaryButton(
                            text = buttonText,
                            onClick = onRequestPermission,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        PrimaryButton(
                            text = buttonText,
                            onClick = onRequestPermission,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        Text(
            text = "Tip",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
}

private fun requestBatteryOptimizationWhitelist(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}

private fun sendTestNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val notification = androidx.core.app.NotificationCompat.Builder(
        context,
        MemoryHelperApplication.REVIEW_CHANNEL_ID
    )
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("Memory Helper test notification")
        .setContentText("Notifications are reaching the device correctly.")
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
        .setCategory(androidx.core.app.NotificationCompat.CATEGORY_REMINDER)
        .setAutoCancel(true)
        .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
        .build()

    notificationManager.notify(9999, notification)
}
