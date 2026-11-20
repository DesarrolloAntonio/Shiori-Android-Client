package com.desarrollodroide.pagekeeper.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colours the Material scheme has no role for.
 *
 * Gold is the fourth hue the Expressive look uses for site monograms and tag colours, next to the
 * teal, indigo and coral the scheme already carries. Without it the fourth slot fell back to
 * surfaceVariant, a flat grey that looked like a missing image rather than a choice.
 *
 * [gold] is the strong tone, for text and icons on the plain surface or on [goldContainer].
 *
 * It stays the same under dynamic colour, like the error colours do: it marks one site or tag
 * among others, and needs to stay apart from whatever the wallpaper makes of the rest.
 */
@Immutable
data class ExtendedColors(
    val gold: Color,
    val goldContainer: Color,
    val onGoldContainer: Color,
)

internal val LightExtendedColors = ExtendedColors(
    gold = Color(0xFF6E5A00),
    goldContainer = Color(0xFFFFE08A),
    onGoldContainer = Color(0xFF231B00),
)

internal val DarkExtendedColors = ExtendedColors(
    gold = Color(0xFFE9C349),
    goldContainer = Color(0xFF554500),
    onGoldContainer = Color(0xFFFFE08A),
)

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
