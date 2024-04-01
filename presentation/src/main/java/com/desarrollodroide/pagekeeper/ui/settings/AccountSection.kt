package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AccountSection(
    onLogout: () -> Unit,
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSeverSettings: () -> Unit,
    onSendFeedbackEmail: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 5.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleSmall
            )
            ClickableOption(
                Item(
                    title = "Server Settings Guide",
                    icon = Icons.Filled.Dns,
                    onClick =  onNavigateToSeverSettings)
            )
            ClickableOption(
                Item(
                    title = "Send Feedback",
                    icon = Icons.Filled.Feedback,
                    onClick = onSendFeedbackEmail
                )
            )
            ClickableOption(
                Item(
                    title = "Terms of Use",
                    icon = Icons.Filled.Gavel,
                    onClick =  onNavigateToTermsOfUse)
            )
            ClickableOption(
                Item(title ="Privacy policy",
                    icon = Icons.Filled.Security,
                    onClick =  onNavigateToPrivacyPolicy)
            )
            ClickableOption(
                Item("Logout",
                    icon =  Icons.Filled.Logout,
                    onClick = onLogout),
                color = Color.Red
            )
        }
    }
}
