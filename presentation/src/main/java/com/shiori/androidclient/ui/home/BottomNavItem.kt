package com.shiori.androidclient.ui.home

import androidx.compose.ui.graphics.vector.ImageVector
import com.shiori.androidclient.navigation.NavItem

data class BottomNavItem(
    val name: String,
    val navItem: NavItem,
    val icon: ImageVector,
)