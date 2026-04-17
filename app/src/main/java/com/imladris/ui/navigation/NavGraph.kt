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
import com.imladris.feature.hall.HallOfImladrisScreen
import com.imladris.feature.library.LibraryScreen
import com.imladris.feature.reader.ReaderScreen
import com.imladris.feature.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.util.Base64

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Hall : Screen("hall", "Sanctuary", Icons.Default.Home)
    object Library : Screen("library", "Library", Icons.Default.List)
    object Analytics : Screen("analytics", "Insights", Icons.Default.Info)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun ImladrisNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Hall.route) {
        composable(Screen.Hall.route) {
            HallOfImladrisScreen(
                onArtifactClick = { title, uri ->
                    val safeTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                    val safeUri = Base64.encodeToString(uri.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
                    navController.navigate("reader/$safeTitle/$safeUri")
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Library.route) {
            LibraryScreen(
                onArtifactClick = { title, uri ->
                    val safeTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
                    val safeUri = Base64.encodeToString(uri.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
                    navController.navigate("reader/$safeTitle/$safeUri")
                }
            )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "reader/{title}/{uri}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("uri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title")?.let { 
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString()) 
            } ?: "Untitled"
            
            val safeUri = backStackEntry.arguments?.getString("uri")
            val uri = safeUri?.let { 
                String(Base64.decode(it, Base64.URL_SAFE)) 
            }

            ReaderScreen(
                title = title,
                uriString = uri,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
