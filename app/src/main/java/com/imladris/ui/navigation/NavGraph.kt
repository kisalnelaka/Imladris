package com.imladris.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.imladris.feature.analytics.AnalyticsScreen
import com.imladris.feature.graph.KnowledgeGraphScreen
import com.imladris.feature.hall.HallOfImladrisScreen
import com.imladris.feature.library.LibraryScreen
import com.imladris.feature.reader.ReaderScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
            HallOfImladrisScreen(
                onArtifactClick = { title, uri ->
                    val encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8.toString())
                    navController.navigate("reader/$title/$encodedUri")
                }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                onArtifactClick = { title, uri ->
                    val encodedUri = URLEncoder.encode(uri, StandardCharsets.UTF_8.toString())
                    navController.navigate("reader/$title/$encodedUri")
                }
            )
        }
        composable(Screen.Graph.route) {
            KnowledgeGraphScreen()
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }
        composable(
            route = "reader/{title}/{uri}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("uri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Untitled"
            val encodedUri = backStackEntry.arguments?.getString("uri")
            val uri = encodedUri?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) }
            ReaderScreen(
                title = title,
                uriString = uri,
                onBack = { navController.popBackStack() },
                onLibraryClick = { navController.navigate(Screen.Library.route) },
                onGraphClick = { navController.navigate(Screen.Graph.route) }
            )
        }
    }
}
