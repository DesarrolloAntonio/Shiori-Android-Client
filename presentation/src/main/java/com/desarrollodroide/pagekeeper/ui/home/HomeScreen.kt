package com.desarrollodroide.pagekeeper.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.androidx.compose.get
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.pagekeeper.navigation.NavItem
import com.desarrollodroide.pagekeeper.ui.feed.FeedScreen
import com.desarrollodroide.pagekeeper.ui.feed.FeedViewModel
import com.desarrollodroide.pagekeeper.ui.settings.SettingsScreen

@Composable
fun HomeScreen(
    feedViewModel: FeedViewModel,
    goToLogin: () -> Unit,
    onFinish: () -> Unit,
    openUrlInBrowser: (String) -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    BackHandler {
        onFinish()
    }
    Scaffold(
        bottomBar = {
            androidx.compose.material3.NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                bottomNavItems.forEach { item ->
                    val selected = item.navItem.route == backStackEntry.value?.destination?.route
                    NavigationBarItem(
                        colors = androidx.compose.material3.NavigationBarItemDefaults
                            .colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    LocalAbsoluteTonalElevation.current
                                )
                            ),
                        selected = selected,
                        onClick = {
                            navController.navigate(item.navItem.route) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                        },
                        label = {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = "${item.name} Icon",
                            )
                        }
                    )
                }
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
            ) {
                NavHost(navController, startDestination = NavItem.HomeNavItem.route) {
                    composable(NavItem.HomeNavItem.route) {
                        FeedScreen(
                            feedViewModel = feedViewModel,
                            goToLogin = goToLogin,
                            openUrlInBrowser = openUrlInBrowser
                        )
                    }

                    composable(NavItem.SettingsNavItem.route) {
                        SettingsScreen(
                            settingsViewModel = get(),
                            goToLogin = goToLogin
                        )
                    }
                }
            }
        }
    )
}

val bottomNavItems = listOf(
    BottomNavItem(
        name = "Bookmarks",
        navItem = NavItem.HomeNavItem,
        icon = Icons.Rounded.Newspaper,
    ),

    BottomNavItem(
        name = "Settings",
        navItem = NavItem.SettingsNavItem,
        icon = Icons.Rounded.Settings,
    )
)
