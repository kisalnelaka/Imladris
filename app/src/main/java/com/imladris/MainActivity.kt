package com.imladris

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.imladris.core.ui.theme.ImladrisTheme
import com.imladris.feature.hall.HallOfImladrisScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ImladrisTheme {
                val navController = androidx.navigation.compose.rememberNavController()
                val items = listOf(Screen.Hall, Screen.Graph, Screen.Analytics)
                
                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = com.imladris.core.ui.theme.MidnightBlue,
                            contentColor = com.imladris.core.ui.theme.SoftGold
                        ) {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    selected = false, // Simplified for brevity
                                    onClick = { navController.navigate(screen.route) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ImladrisNavGraph(navController)
                    }
                }
            }
        }
    }
}
