package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * A settings row.
 *
 * Built on M3 [ListItem] rather than a hand-assembled [androidx.compose.foundation.layout.Row]:
 * that gives the spec's leading-icon spacing, minimum touch height and text roles for free. The
 * container is transparent so the enclosing [SettingsGroup] card provides the surface.
 */
@Composable
fun ClickableOption(
    title: String,
    icon: ImageVector,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = if (subtitle.isNotEmpty()) {
            { Text(subtitle, style = MaterialTheme.typography.bodyMedium) }
        } else null,
    )
}

@Composable
fun ClickableOption(
    item: Item,
    modifier: Modifier = Modifier,
) {
    ClickableOption(
        title = item.title,
        icon = item.icon,
        subtitle = item.subtitle,
        modifier = modifier,
        onClick = item.onClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun ClickableOptionPreview() {
    ShioriTheme {
        SettingsGroup(title = "Account") {
            ClickableOption(
                title = "Settings",
                subtitle = "Set your preferences",
                icon = Icons.Default.Settings,
                onClick = {},
            )
            ClickableOption(
                title = "Profile",
                icon = Icons.Default.Person,
                onClick = {},
            )
        }
    }
}
