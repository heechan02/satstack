package com.example.satstack

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
// androidx.activity:activity-compose
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// androidx.compose.material:material-icons-extended
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
// androidx.navigation:navigation-compose
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.satstack.data.DataStoreKeys
import com.example.satstack.data.appDataStore
import com.example.satstack.nav.Route
import com.example.satstack.ui.analytics.AnalyticsScreen
import com.example.satstack.ui.dashboard.DashboardScreen
import com.example.satstack.ui.settings.SettingsScreen
import com.example.satstack.ui.theme.SatStackTheme
import com.example.satstack.ui.onboarding.OnboardingScreen
import com.example.satstack.ui.vault.VaultScreen
import kotlinx.coroutines.flow.map

class MainActivity : FragmentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val darkMode by context.appDataStore.data
                .map { it[DataStoreKeys.DARK_MODE] ?: true }
                .collectAsState(initial = true)
            val onboardingDone by context.appDataStore.data
                .map { it[DataStoreKeys.ONBOARDING_COMPLETE] ?: false }
                .collectAsState(initial = null)
            SatStackTheme(darkTheme = darkMode) {
                if (onboardingDone == null) return@SatStackTheme

                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val showChrome = currentRoute != Route.VAULT.name && currentRoute != Route.ONBOARDING.name

                val navItems = listOf(
                    Triple(Route.DASHBOARD, "Dashboard", Icons.Filled.Home),
                    Triple(Route.ANALYTICS, "Analytics", Icons.Filled.Analytics),
                    Triple(Route.SETTINGS, "Settings", Icons.Filled.Settings)
                )

                Scaffold(
                    topBar = {
                        if (showChrome) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        "Sat Stack",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                            )
                        }
                    },
                    bottomBar = {
                        if (showChrome) {
                            NavigationBar {
                                navItems.forEach { (route, label, icon) ->
                                    NavigationBarItem(
                                        selected = currentRoute == route.name,
                                        onClick = {
                                            navController.navigate(route.name) {
                                                popUpTo(Route.DASHBOARD.name) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(28.dp)) },
                                        label = { Text(label, fontSize = 13.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.primary,
                                            selectedIconColor = Color.White,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (onboardingDone!!) Route.VAULT.name else Route.ONBOARDING.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Route.ONBOARDING.name) {
                            OnboardingScreen(onComplete = {
                                navController.navigate(Route.VAULT.name) {
                                    popUpTo(Route.ONBOARDING.name) { inclusive = true }
                                }
                            })
                        }
                        composable(Route.VAULT.name) {
                            VaultScreen(onAuthSuccess = {
                                navController.navigate(Route.DASHBOARD.name) {
                                    popUpTo(Route.VAULT.name) { inclusive = true }
                                }
                            })
                        }
                        composable(Route.DASHBOARD.name) {
                            DashboardScreen()
                        }
                        composable(Route.ANALYTICS.name) { AnalyticsScreen() }
                        composable(Route.SETTINGS.name) { SettingsScreen() }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MILESTONE_CHANNEL_ID,
                "Milestone Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifies when your sats stack reaches a milestone" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val MILESTONE_CHANNEL_ID = "milestone_channel"
    }
}
