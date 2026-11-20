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

/**
 * Whether the login form should sit beside its branding rather than underneath it.
 *
 * Stacked, the form needs about 370dp under the hero to show the Sign in button, and the hero
 * shrinks from 360dp to 200dp to make room, so stacked fits from roughly 570dp of height. Below
 * 640dp it fits only with the hero squeezed, and a landscape tablet (540dp to 600dp) looks better
 * with the hero as a panel beside the form than as a strip above it.
 *
 * Beside, the tallest column is the form, and the window pays for it out of the width it already
 * had going spare: at 960dp wide the form is capped at 460dp and the rest of the row is the hero.
 *
 * Takes measurements rather than a window size class so the decision follows the space this
 * composable actually got, and so a test can make it without a window.
 */
fun shouldPlaceBrandingBeside(availableWidth: Dp, availableHeight: Dp): Boolean =
    availableWidth >= BrandingBesideMinWidth && availableHeight < BrandingBesideMaxHeight

/** Below this there is not enough width for two columns that are both worth reading. */
val BrandingBesideMinWidth: Dp = 600.dp

/** At or above this the stacked layout fits, and stacked is the better looking of the two. */
val BrandingBesideMaxHeight: Dp = 640.dp

/**
 * Whether a form should run in two columns rather than one tall one.
 *
 * A tablet in landscape is about 960dp wide and 540dp tall. The bookmark editor's single capped
 * column used 460dp of that width and left the rest blank, and still did not fit: Tags was cut in
 * half by the save bar and its suggestions were off screen entirely. Two columns spend the width
 * the window actually has instead of the height it does not.
 *
 * The width floor is two columns' worth side by side plus the gap between them; below it the two
 * would each be narrower than a phone.
 *
 * Both arguments are the space the form is actually given, not the size of the window: the app
 * bar, the pinned save bar and the insets come off first. An unfolded foldable is 883dp tall and
 * hands the form 615dp of it, which is why it splits too — and it should, because the add form
 * with its three switches wants about 650dp in one column.
 */
fun shouldSplitFormIntoTwoColumns(availableWidth: Dp, availableHeight: Dp): Boolean =
    availableWidth >= TwoColumnFormMinWidth && availableHeight < TwoColumnFormMaxHeight

/** Two readable columns and a gap. Below this, one column is the better use of the space. */
val TwoColumnFormMinWidth: Dp = 720.dp

/** At or above this the single column fits, and one column is easier to fill in than two. */
val TwoColumnFormMaxHeight: Dp = 640.dp
