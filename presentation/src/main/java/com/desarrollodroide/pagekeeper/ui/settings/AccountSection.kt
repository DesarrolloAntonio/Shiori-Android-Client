package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AccountSection(
    serverUrl: String,
    onLogout: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSeverSettings: () -> Unit,
    onSendFeedbackEmail: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsGroup(title = "Account", modifier = modifier) {
        ClickableOption(
            title = "Logout",
            subtitle = serverUrl,
            icon = Icons.AutoMirrored.Filled.Logout,
            onClick = onLogout
        )
        ClickableOption(
            title = "Server Settings Guide",
            icon = Icons.Filled.Dns,
            onClick = onNavigateToSeverSettings
        )
        ClickableOption(
            title = "Source Code",
            icon = Icons.Filled.Code,
            onClick = onNavigateToSourceCode
        )
        ClickableOption(
            title = "Send Feedback",
            icon = Icons.Filled.Feedback,
            onClick = onSendFeedbackEmail
        )
        ClickableOption(
            title = "Terms of Use",
            icon = Icons.Filled.Gavel,
            onClick = onNavigateToTermsOfUse
        )
        ClickableOption(
            title = "Privacy policy",
            icon = Icons.Filled.Security,
            onClick = onNavigateToPrivacyPolicy
        )
    }
}
