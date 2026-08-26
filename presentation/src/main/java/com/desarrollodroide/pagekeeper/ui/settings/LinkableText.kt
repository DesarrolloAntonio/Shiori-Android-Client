package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.desarrollodroide.pagekeeper.extensions.openUrlInBrowser

/**
 * A tappable link.
 *
 * Uses [LinkAnnotation] inside a plain [Text] rather than the deprecated `ClickableText`; the link
 * annotation is what gives the range a proper "link" role for accessibility services and lets the
 * text engine style hover/press states.
 */
@Composable
fun LinkableText(
    text: String,
    url: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    val annotatedText = buildAnnotatedString {
        withLink(
            LinkAnnotation.Clickable(
                tag = url,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                    )
                ),
                linkInteractionListener = { context.openUrlInBrowser(url) },
            )
        ) {
            append(text)
        }
    }
    Text(
        modifier = modifier,
        text = annotatedText,
        style = MaterialTheme.typography.titleMedium,
    )
}
