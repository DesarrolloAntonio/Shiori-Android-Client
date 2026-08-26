package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DebugSection(
    onNavigateToLogs: () -> Unit,
    onViewLastCrash: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsGroup(title = "Debug", modifier = modifier) {
        ClickableOption(
            title = "View network logs",
            icon = Icons.Default.Code,
            onClick = onNavigateToLogs
        )
        ClickableOption(
            title = "View last crash",
            icon = Icons.Default.BugReport,
            onClick = onViewLastCrash
        )
    }
}
