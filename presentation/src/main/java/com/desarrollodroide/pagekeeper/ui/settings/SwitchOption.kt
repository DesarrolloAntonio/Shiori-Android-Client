package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
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
    val accent = LocalSettingsAccent.current ?: SettingsAccents.visual
    SettingsRow(
        icon = icon,
        title = title,
        subtitle = subtitle,
        // One control: the row is the toggle and the Switch only draws it. With both taking clicks
        // a screen reader stopped twice per setting, and the second stop had no name.
        modifier = modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                // On, the thumb takes the group's colour and a check, so it reads at a glance.
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                        )
                    }
                } else null,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = accent.container,
                    checkedIconColor = accent.content,
                ),
            )
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
