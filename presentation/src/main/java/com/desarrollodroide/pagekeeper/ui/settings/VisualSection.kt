package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.data.helpers.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun VisualSection(
    themeMode: MutableStateFlow<ThemeMode>,
    dynamicColors: MutableStateFlow<Boolean>,
    modifier: Modifier = Modifier,
) {
    SettingsGroup(title = "Visual", modifier = modifier, accent = SettingsAccents.visual) {
        ThemeOption(
            item = Item("Theme", Icons.Filled.Palette, onClick = {}),
            initialThemeMode = themeMode
        )
        val dynamicColorItem = Item(
            title = "Use dynamic colors",
            icon = Icons.Filled.FormatColorFill,
            switchState = dynamicColors
        )
        SwitchOption(
            item = dynamicColorItem,
            switchState = dynamicColors
        )
    }
}

private data class ThemeChoice(val mode: ThemeMode, val label: String, val icon: ImageVector)

private val ThemeChoices = listOf(
    ThemeChoice(ThemeMode.LIGHT, "Light", Icons.Outlined.LightMode),
    ThemeChoice(ThemeMode.DARK, "Dark", Icons.Outlined.DarkMode),
    ThemeChoice(ThemeMode.AUTO, "Auto", Icons.Outlined.BrightnessAuto),
)

/**
 * The three themes side by side, the chosen one filled and fully round.
 *
 * It used to be one row that cycled dark, light, auto on each tap, so reaching a given theme could
 * take two taps and the row never said what the next tap would do.
 */
@Composable
fun ThemeOption(
    item: Item,
    initialThemeMode: MutableStateFlow<ThemeMode>,
    modifier: Modifier = Modifier,
) {
    val themeMode by initialThemeMode.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(scheme.surfaceContainer)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsIconChip(item.icon)
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ThemeChoices.forEachIndexed { index, choice ->
                val selected = choice.mode == themeMode
                val shape = when {
                    selected -> RoundedCornerShape(22.dp)
                    index == 0 -> RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp, topEnd = 6.dp, bottomEnd = 6.dp)
                    index == ThemeChoices.lastIndex -> RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 22.dp, bottomEnd = 22.dp)
                    else -> RoundedCornerShape(6.dp)
                }
                Surface(
                    selected = selected,
                    onClick = {
                        initialThemeMode.value = choice.mode
                        item.onClick()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .semantics { role = Role.RadioButton },
                    shape = shape,
                    color = if (selected) scheme.primary else scheme.surfaceContainerLowest,
                    contentColor = if (selected) scheme.onPrimary else scheme.onSurface,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(choice.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = choice.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
