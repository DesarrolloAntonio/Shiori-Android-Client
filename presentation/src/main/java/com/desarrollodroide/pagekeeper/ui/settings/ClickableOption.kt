package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * A settings row that does something when tapped.
 *
 * [trailingIcon] says what kind of thing: a chevron for another screen (the default), an arrow out
 * of the app for a page in the browser, nothing for an action that happens right here.
 */
@Composable
fun ClickableOption(
    title: String,
    icon: ImageVector,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    onClick: () -> Unit,
) {
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        modifier = modifier.clickable(onClick = onClick),
        trailing = trailingIcon?.let { trailing ->
            {
                Icon(
                    imageVector = trailing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
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
