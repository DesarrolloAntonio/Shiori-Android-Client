package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.ui.theme.keyedColors

/**
 * What a card shows when the server had no thumbnail for the page.
 *
 * The site's initial on one of the Expressive shapes, over a tonal panel, with the site under it,
 * filling the same slot a cover would. A site always gets the same colour and shape, so its cards
 * are recognisable at a glance, and a feed of image-less bookmarks looks deliberate rather than
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
    val style = placeholderStyle(host)

    Box(
        modifier = modifier.background(style.container),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SiteMonogram(
                host = host,
                shape = style.shape,
                size = 88.dp,
                fill = style.onContainer,
                letterColor = style.container,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = host.ifEmpty { "no link" },
                color = style.onContainer,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The compact list's thumbnail when there is no image: the same monogram, on its own. */
@Composable
fun BookmarkThumbnailPlaceholder(
    url: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val host = hostOf(url)
    val style = placeholderStyle(host)
    SiteMonogram(
        host = host,
        shape = style.shape,
        size = size,
        fill = style.container,
        letterColor = style.onContainer,
        modifier = modifier,
    )
}

/**
 * A name's initial on its keyed shape, in the strong colour of its pair. For tags, which have no
 * site: the tag's row in the Tags screen gets the same colour its chips get on the cards.
 */
@Composable
fun KeyedMonogram(
    key: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val style = placeholderStyle(key)
    SiteMonogram(
        host = key,
        shape = style.shape,
        size = size,
        fill = style.onContainer,
        letterColor = style.container,
        modifier = modifier,
    )
}

@Composable
private fun SiteMonogram(
    host: String,
    shape: Shape,
    size: Dp,
    fill: Color,
    letterColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(fill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = host.firstOrNull()?.toString() ?: "?",
            color = letterColor,
            style = if (size >= 80.dp) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

private class PlaceholderStyle(val container: Color, val onContainer: Color, val shape: Shape)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun placeholderStyle(host: String): PlaceholderStyle {
    // Gold rather than surfaceVariant in the fourth slot: the grey read as a missing image.
    val colors = keyedColors(host)
    val polygon = when (placeholderPaletteIndex(host, 4)) {
        0 -> MaterialShapes.Cookie9Sided
        1 -> MaterialShapes.Clover4Leaf
        2 -> MaterialShapes.Sunny
        else -> MaterialShapes.Flower
    }
    return PlaceholderStyle(colors.container, colors.content, polygon.toShape())
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
