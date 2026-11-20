package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.pagekeeper.ui.theme.LocalExtendedColors

/**
 * The colour a settings group is drawn in: its heading, and the chip behind each row's icon (and
 * the thumb of a switch that is on). Each group gets its own, so a long screen reads as a few
 * distinct blocks rather than one grey list.
 */
@Immutable
data class SettingsAccent(val heading: Color, val container: Color, val content: Color)

object SettingsAccents {
    val bookmarkList: SettingsAccent
        @Composable get() = MaterialTheme.colorScheme.let { SettingsAccent(it.tertiary, it.tertiaryContainer, it.tertiary) }

    val visual: SettingsAccent
        @Composable get() = MaterialTheme.colorScheme.let { SettingsAccent(it.secondary, it.secondaryContainer, it.secondary) }

    val defaults: SettingsAccent
        @Composable get() = LocalExtendedColors.current.let { SettingsAccent(it.gold, it.goldContainer, it.gold) }

    val data: SettingsAccent
        @Composable get() = MaterialTheme.colorScheme.let { SettingsAccent(it.primary, it.primaryContainer, it.primary) }

    val debug: SettingsAccent
        @Composable get() = MaterialTheme.colorScheme.let { SettingsAccent(it.onSurface, it.inverseSurface, it.inverseOnSurface) }

    val about: SettingsAccent
        @Composable get() = MaterialTheme.colorScheme.let {
            SettingsAccent(it.primary, lerp(it.surfaceContainerHigh, it.primaryContainer, 0.45f), it.primary)
        }
}

/** The accent of the group a row sits in; rows outside a group fall back to [SettingsAccents.visual]. */
val LocalSettingsAccent = staticCompositionLocalOf<SettingsAccent?> { null }

/**
 * A titled group of settings rows.
 *
 * The rows are separate tonal blocks 2dp apart, square-ish between each other and round at the
 * ends of the group: the connected look the Expressive lists use. It scales to any number of rows,
 * and a single row comes out fully round.
 */
@Composable
fun SettingsGroup(
    title: String,
    modifier: Modifier = Modifier,
    accent: SettingsAccent = SettingsAccents.visual,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = accent.heading,
            modifier = Modifier
                .padding(start = 8.dp, bottom = 8.dp)
                .semantics { heading() },
        )
        SettingsRows(accent = accent, content = content)
    }
}

/** The connected rows of a group, without a heading. The bookmark editor's options use it too. */
@Composable
fun SettingsRows(
    accent: SettingsAccent,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalSettingsAccent provides accent) {
        // The group's clip rounds the outer corners; each row's own 6dp clip rounds the inner ones.
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
    }
}

/** One row: the icon on its coloured chip, title and subtitle, and whatever goes at the end. */
@Composable
internal fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(modifier)
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsIconChip(icon)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
internal fun SettingsIconChip(icon: ImageVector) {
    val accent = LocalSettingsAccent.current ?: SettingsAccents.visual
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(accent.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent.content,
            modifier = Modifier.size(22.dp),
        )
    }
}
