package com.example.memoryhelper

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.memoryhelper.ui.screens.flashcard.FlashcardScreen
import com.example.memoryhelper.ui.screens.home.HomeScreen
import com.example.memoryhelper.ui.screens.home.HomeViewModel
import com.example.memoryhelper.ui.screens.settings.SettingsScreen
import com.example.memoryhelper.ui.screens.stats.StatsScreen
import com.example.memoryhelper.ui.screens.todo.TodoScreen
import com.example.memoryhelper.ui.theme.MemoryHelperTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Navigation destinations
 */
sealed class Screen(val route: String, val titleRes: Int) {
    data object Home : Screen("home", R.string.nav_home)
    data object Todo : Screen("todo", R.string.nav_todo)
    data object Stats : Screen("stats", R.string.nav_stats)
    data object Settings : Screen("settings", R.string.nav_settings)
    data object Flashcard : Screen("flashcard", R.string.nav_home)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoryHelperTheme {
                MainContent()
            }
        }
    }
}

@Composable
private fun MainContent() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Permission states
    var notificationPermissionGranted by remember {
        mutableStateOf(checkNotificationPermission(context))
    }
    var exactAlarmPermissionGranted by remember {
        mutableStateOf(checkExactAlarmPermission(context))
    }
    var showPermissionBanner by remember { mutableStateOf(true) }

    // Track current destination
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
    }

    // Check permissions on resume
    LaunchedEffect(Unit) {
        notificationPermissionGranted = checkNotificationPermission(context)
        exactAlarmPermissionGranted = checkExactAlarmPermission(context)
    }

    // Request notification permission on first launch (Android 13+)
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationPermissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = stringResource(R.string.nav_home)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate(Screen.Todo.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(R.string.nav_todo)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_todo)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_bar_chart),
                            contentDescription = stringResource(R.string.nav_stats)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_stats)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.nav_settings)
                        )
                    },
                    label = { Text(stringResource(R.string.nav_settings)) }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Show permission banner if any permission is missing
            if (showPermissionBanner && (!notificationPermissionGranted || !exactAlarmPermissionGranted)) {
                PermissionBanner(
                    notificationPermissionGranted = notificationPermissionGranted,
                    exactAlarmPermissionGranted = exactAlarmPermissionGranted,
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onRequestExactAlarmPermission = {
                        openExactAlarmSettings(context)
                    },
                    onDismiss = { showPermissionBanner = false },
                    onRefresh = {
                        notificationPermissionGranted = checkNotificationPermission(context)
                        exactAlarmPermissionGranted = checkExactAlarmPermission(context)
                    }
                )
            }

            // Navigation Host
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToFlashcard = { items ->
                            navController.navigate(Screen.Flashcard.route)
                        }
                    )
                }
                composable(Screen.Todo.route) {
                    TodoScreen()
                }
                composable(Screen.Stats.route) {
                    StatsScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                composable(Screen.Flashcard.route) {
                    val homeEntry = remember(navController.currentBackStackEntry) {
                        navController.getBackStackEntry(Screen.Home.route)
                    }
                    val homeViewModel: com.example.memoryhelper.ui.screens.home.HomeViewModel =
                        androidx.hilt.navigation.compose.hiltViewModel(homeEntry)
                    val uiState by homeViewModel.uiState.collectAsState()
                    val dueItems = uiState.overdueItems + uiState.todayItems.filter {
                        it.nextReviewTime <= System.currentTimeMillis()
                    }

                    FlashcardScreen(
                        items = dueItems,
                        onRemember = { homeViewModel.markAsRemembered(it) },
                        onForgot = { homeViewModel.markAsForgot(it) },
                        onComplete = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(
    notificationPermissionGranted: Boolean,
    exactAlarmPermissionGranted: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onRequestExactAlarmPermission: () -> Unit,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.permissions_required),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.permissions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${stringResource(R.string.permission_notifications)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onRequestNotificationPermission
                    ) {
                        Text(stringResource(R.string.grant))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Exact alarm permission (Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !exactAlarmPermissionGranted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${stringResource(R.string.permission_exact_alarms)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = onRequestExactAlarmPermission
                    ) {
                        Text(stringResource(R.string.settings))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dismiss))
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onRefresh) {
                    Text(stringResource(R.string.refresh))
                }
            }
        }
    }
}

/**
 * Checks if notification permission is granted.
 * Always returns true for Android 12 and below.
 */
private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Not required before Android 13
    }
}

/**
 * Checks if exact alarm permission is granted.
 * Always returns true for Android 11 and below.
 */
private fun checkExactAlarmPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.canScheduleExactAlarms() == true
    } else {
        true // Not required before Android 12
    }
}

/**
 * Opens the exact alarm settings for the app (Android 12+).
 */
private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
}
