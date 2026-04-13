package com.imladris.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.imladris.feature.analytics.AnalyticsScreen
import com.imladris.feature.graph.KnowledgeGraphScreen
import com.imladris.feature.hall.HallOfImladrisScreen
import com.imladris.feature.reader.ReaderScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Hall : Screen("hall", "Hall", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.List)
    object Graph : Screen("graph", "Graph", Icons.Default.Share)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Info)
}

@Composable
fun ImladrisNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Hall.route) {
        composable(Screen.Hall.route) {
            HallOfImladrisScreen()
        }
        composable(Screen.Graph.route) {
            KnowledgeGraphScreen()
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }
        composable("reader/{title}") { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Untitled"
            ReaderScreen(title)
        }
    }
}
