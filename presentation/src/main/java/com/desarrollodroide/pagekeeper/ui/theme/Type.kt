package com.desarrollodroide.pagekeeper.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.desarrollodroide.pagekeeper.R

// Roboto Flex: one variable font (SIL OFL 1.1; the licence travels in the font's own name table),
// read at different settings. minSdk is 26, so the variation axes always apply.
//
// The design was drawn in a browser, which sets the optical size axis to the type size on its own:
// a 36sp title gets the 36 cut, a 57sp one the 57 cut. Compose does not, so each display and
// headline size gets a family read at its own size. One family at 72 for all of them made a
// four-line article title in displaySmall come out crushed and too black.
private fun flex(weight: Int, opticalSize: Float? = null) = Font(
    R.font.roboto_flex,
    weight = FontWeight(weight),
    variationSettings = if (opticalSize == null) {
        FontVariation.Settings(FontVariation.weight(weight))
    } else {
        FontVariation.Settings(FontVariation.weight(weight), FontVariation.Setting("opsz", opticalSize))
    },
)

// Every weight the screens ask for, up to Black: a family without the requested weight falls back
// to the nearest one it has, and a "Black" title would quietly come out ExtraBold.
private val TextWeights = listOf(400, 500, 600, 700, 800, 900)

private fun flexFamily(opticalSize: Float?) = FontFamily(TextWeights.map { flex(it, opticalSize) })

/** Titles, body and labels: the font's default optical size, made for text. */
val RobotoFlex = flexFamily(null)

private val RobotoFlex24 = flexFamily(24f)
private val RobotoFlex28 = flexFamily(28f)
private val RobotoFlex32 = flexFamily(32f)
private val RobotoFlex36 = flexFamily(36f)
private val RobotoFlex45 = flexFamily(45f)

/** displayLarge, and the large counts that are set from it at 60 to 76sp. */
val RobotoFlexDisplay = flexFamily(57f)

/**
 * Material 3 Expressive type scale.
 *
 * Expressive typography leans on weight contrast rather than size alone: display and headline
 * roles are set in heavy weights with tight tracking so they carry the screen, while body and
 * label roles stay at readable, normal-weight settings. The baseline M3 scale used everything
 * at SemiBold/Bold, which flattened the hierarchy — body text should not shout.
 */
val shioriTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlexDisplay,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = RobotoFlex45,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = RobotoFlex36,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex32,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFlex28,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.2).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFlex24,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
