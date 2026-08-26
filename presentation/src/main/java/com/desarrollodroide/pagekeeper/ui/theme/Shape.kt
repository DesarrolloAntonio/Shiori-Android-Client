package com.desarrollodroide.pagekeeper.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shape scale.
 *
 * Expressive pushes corners noticeably rounder than baseline M3: containers read as soft,
 * pill-adjacent volumes rather than boxes. The scale below follows the expressive corner
 * tokens (4 / 12 / 16 / 24 / 32) instead of the baseline (4 / 8 / 12 / 16 / 28).
 */
val shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Corner tokens the expressive scale adds on top of [Shapes], exposed for direct use. */
object ExpressiveShapes {
    val largeIncreased = RoundedCornerShape(20.dp)
    val extraLargeIncreased = RoundedCornerShape(36.dp)
    val extraExtraLarge = RoundedCornerShape(48.dp)
}
