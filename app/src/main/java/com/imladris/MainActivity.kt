package com.imladris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.imladris.core.ui.theme.*
import com.imladris.ui.navigation.ImladrisNavGraph
import com.imladris.ui.navigation.Screen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImladrisTheme {
                val navController = rememberNavController()
                val items = listOf(Screen.Hall, Screen.Library, Screen.Graph, Screen.Analytics)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                Scaffold(
                    bottomBar = {
                        // Only show bottom bar if not in reader
                        if (currentRoute != null && !currentRoute.startsWith("reader")) {
                            NavigationBar(
                                containerColor = MidnightBlue.copy(alpha = 0.95f),
                                contentColor = CelestialBlue,
                                tonalElevation = 0.dp
                            ) {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                                        selected = currentRoute == screen.route,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = CelestialBlue,
                                            selectedTextColor = CelestialBlue,
                                            unselectedIconColor = SilverGlow.copy(alpha = 0.4f),
                                            unselectedTextColor = SilverGlow.copy(alpha = 0.4f),
                                            indicatorColor = DeepMist
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MidnightBlue
                    ) {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            ImladrisNavGraph(navController)
                        }
                    }
                }
            }
        }
    }
}
