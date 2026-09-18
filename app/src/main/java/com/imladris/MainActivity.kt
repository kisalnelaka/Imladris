package com.imladris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        if (currentRoute != null && !currentRoute.startsWith("reader")) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = Color(0xFF101520).copy(alpha = 0.94f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CelestialBlue.copy(alpha = 0.15f)),
                                    tonalElevation = 6.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    NavigationBar(
                                        containerColor = Color.Transparent,
                                        contentColor = CelestialBlue,
                                        tonalElevation = 0.dp,
                                        modifier = Modifier.height(64.dp)
                                    ) {
                                        items.forEach { screen ->
                                            val selected = currentRoute == screen.route
                                            NavigationBarItem(
                                                icon = { 
                                                    Icon(
                                                        screen.icon, 
                                                        contentDescription = screen.title,
                                                        modifier = Modifier.size(20.dp)
                                                    ) 
                                                },
                                                label = { 
                                                    Text(
                                                        screen.title, 
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                                        )
                                                    ) 
                                                },
                                                selected = selected,
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
                                                    unselectedIconColor = SilverGlow.copy(alpha = 0.45f),
                                                    unselectedTextColor = SilverGlow.copy(alpha = 0.45f),
                                                    indicatorColor = CelestialBlue.copy(alpha = 0.12f)
                                                )
                                            )
                                        }
                                    }
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
