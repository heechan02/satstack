package com.example.satstack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// androidx.compose.material:material-icons-extended
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
// androidx.navigation:navigation-compose
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.satstack.nav.Route
import com.example.satstack.ui.analytics.AnalyticsScreen
import com.example.satstack.ui.dashboard.DashboardScreen
import com.example.satstack.ui.settings.SettingsScreen
import com.example.satstack.ui.theme.SatStackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SatStackTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val navItems = listOf(
                    Triple(Route.DASHBOARD, "Dashboard", Icons.Filled.Home as ImageVector),
                    Triple(Route.ANALYTICS, "Analytics", Icons.Filled.Analytics as ImageVector),
                    Triple(Route.SETTINGS, "Settings", Icons.Filled.Settings as ImageVector)
                )

                Scaffold(
                    bottomBar = {
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
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Route.DASHBOARD.name,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Route.DASHBOARD.name) { DashboardScreen() }
                        composable(Route.ANALYTICS.name) { AnalyticsScreen() }
                        composable(Route.SETTINGS.name) { SettingsScreen() }
                    }
                }
            }
        }
    }
}
