package com.desarrollodroide.pagekeeper.ui.home

import androidx.compose.ui.graphics.vector.ImageVector
import com.desarrollodroide.pagekeeper.navigation.NavItem

data class BottomNavItem(
    val name: String,
    val navItem: NavItem,
    val icon: ImageVector,
)