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
 * Stacked, the screen wants about 680dp of height: logo, heading and subtitle come to roughly
 * 200dp before the card starts, and the card itself is another 330dp. A landscape tablet has
 * 540dp to 600dp, so the Log in button lands under the gesture bar — and on a 1080p tablet only
 * the top edge of it is on screen at all. It does scroll, but nothing on screen says so, which is
 * the worst version of not fitting.
 *
 * Beside, the tallest column is the card, and the window pays for it out of the width it already
 * had going spare: at 960dp wide the form is capped at 460dp and the rest of the row is empty.
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
