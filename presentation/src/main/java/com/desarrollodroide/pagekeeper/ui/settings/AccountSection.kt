package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Which server this device is signed in to, and the way out of it. A card of its own at the top,
 * in the brand colour: it is the one setting that is about the account rather than the app.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountSection(
    serverUrl: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = scheme.primaryContainer,
        contentColor = scheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(scheme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Dns,
                        contentDescription = null,
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Connected to",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        // Without the scheme: on a phone the full url was cut off before the
                        // part that tells one server from another.
                        text = serverUrl.substringAfter("://").trimEnd('/').ifEmpty { "No server" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = scheme.surfaceContainerLowest,
                        contentColor = scheme.onPrimaryContainer,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/** Where the app comes from and the small print. The browser ones say so with an arrow out. */
@Composable
fun AboutSection(
    onNavigateToTermsOfUse: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToSeverSettings: () -> Unit,
    onSendFeedbackEmail: () -> Unit,
    onNavigateToSourceCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsGroup(title = "About", modifier = modifier, accent = SettingsAccents.about) {
        ClickableOption(
            title = "Server Settings Guide",
            icon = Icons.Filled.Dns,
            trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
            onClick = onNavigateToSeverSettings
        )
        ClickableOption(
            title = "Source Code",
            icon = Icons.Filled.Code,
            trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
            onClick = onNavigateToSourceCode
        )
        ClickableOption(
            title = "Send Feedback",
            icon = Icons.Filled.Feedback,
            trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
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
