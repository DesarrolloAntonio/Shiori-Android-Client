package com.desarrollodroide.pagekeeper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.desarrollodroide.pagekeeper.ui.feed.item.placeholderPaletteIndex

/** A container colour and the colour that reads on it. */
data class KeyedColors(val container: Color, val content: Color)

/**
 * The colour a site or a tag always gets.
 *
 * Teal, indigo, coral or gold, chosen from the name so it is the same on every screen and every
 * device. That is what lets a tag chip on a card, the tag's row in the Tags screen and a site's
 * monogram be recognised before they are read.
 */
@Composable
fun keyedColors(key: String): KeyedColors {
    val scheme = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    val palette = listOf(
        KeyedColors(scheme.primaryContainer, scheme.onPrimaryContainer),
        KeyedColors(scheme.secondaryContainer, scheme.onSecondaryContainer),
        KeyedColors(scheme.tertiaryContainer, scheme.onTertiaryContainer),
        KeyedColors(extended.goldContainer, extended.onGoldContainer),
    )
    return palette[placeholderPaletteIndex(key, palette.size)]
}
