package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun SwitchOption(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = if (subtitle.isNotEmpty()) {
            { Text(subtitle, style = MaterialTheme.typography.bodyMedium) }
        } else null,
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
}

@Composable
fun SwitchOption(
    item: Item,
    switchState: MutableStateFlow<Boolean>,
    modifier: Modifier = Modifier,
) {
    // collectAsStateWithLifecycle stops collecting while the screen is in the background;
    // collectAsState kept the subscription alive behind other screens.
    val switchValue by switchState.collectAsStateWithLifecycle()
    SwitchOption(
        title = item.title,
        icon = item.icon,
        subtitle = item.subtitle,
        checked = switchValue,
        modifier = modifier,
        onCheckedChange = { switchState.value = it },
    )
}
