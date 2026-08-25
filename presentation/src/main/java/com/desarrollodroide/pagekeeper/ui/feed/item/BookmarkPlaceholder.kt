package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * What a card shows when the server had no thumbnail for the page.
 *
 * A tinted panel with the missing-image glyph and the site it came from, filling the same slot a
 * thumbnail would. A site always gets the same colour, so a card is recognisable at a glance
 * before the title has been read, and a feed of image-less bookmarks looks deliberate rather than
 * unfinished. Filling the slot also keeps such a card the same height as the one beside it.
 *
 * Colours come from the theme's own roles rather than a generated hue, so it follows light, dark
 * and dynamic colour instead of fighting them.
 */
@Composable
fun BookmarkImagePlaceholder(
    url: String,
    modifier: Modifier = Modifier,
) {
    val host = hostOf(url)
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(
        scheme.primaryContainer to scheme.onPrimaryContainer,
        scheme.secondaryContainer to scheme.onSecondaryContainer,
        scheme.tertiaryContainer to scheme.onTertiaryContainer,
        scheme.surfaceVariant to scheme.onSurfaceVariant,
    )
    val (container, onContainer) = palette[placeholderPaletteIndex(host, palette.size)]

    Box(
        modifier = modifier.background(
            // A flat fill at this size reads as a missing image. The wash gives it enough shape to
            // look like a deliberate cover.
            Brush.linearGradient(
                listOf(container, container.copy(alpha = 0.55f)),
            )
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.BrokenImage,
                contentDescription = null,
                tint = onContainer.copy(alpha = 0.55f),
                modifier = Modifier.size(44.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = host.ifEmpty { "no link" },
                color = onContainer.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * The host, without scheme or a leading www.
 *
 * Returns the input trimmed when there is nothing that looks like a host, which is what a bookmark
 * saved from a share sheet with something odd in it will hit.
 */
fun hostOf(url: String): String {
    val withoutScheme = url.trim().substringAfter("://", url.trim())
    val host = withoutScheme.substringBefore('/').substringBefore('?').substringBefore('#')
    return host.removePrefix("www.").lowercase()
}

/**
 * Which palette entry a host gets.
 *
 * Deterministic, so the same site is always the same colour and stays recognisable between
 * sessions and across devices. String.hashCode is stable across JVM versions by specification,
 * which the default object hash is not.
 */
fun placeholderPaletteIndex(host: String, paletteSize: Int): Int {
    if (paletteSize <= 0) return 0
    val hash = host.hashCode()
    return ((hash % paletteSize) + paletteSize) % paletteSize
}
