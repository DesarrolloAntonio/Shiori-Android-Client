package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Caps content width and centres it.
 *
 * On a tablet the window is around 1280dp wide. Left to fill it, a form's text fields and buttons
 * stretch the full width and a paragraph runs to line lengths nobody can read. M3 asks for content
 * to be constrained on large windows rather than stretched, so anything that is a single column of
 * content gets a ceiling and sits in the middle.
 *
 * Lists that genuinely use the extra width, like the feed's adaptive grid, should not use this.
 */
@Composable
fun ResponsiveContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = ContentMaxWidth,
    alignment: Alignment = Alignment.TopCenter,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = alignment) {
        Box(
            modifier = Modifier.widthIn(max = maxWidth),
            content = content,
        )
    }
}

/** Comfortable reading width for a single column of content. */
val ContentMaxWidth: Dp = 560.dp

/** Forms want to be narrower still; a full width text field on a tablet looks broken. */
val FormMaxWidth: Dp = 460.dp
