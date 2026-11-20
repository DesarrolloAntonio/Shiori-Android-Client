package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.pagekeeper.ui.theme.LocalExtendedColors
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/** The library with nothing in it at all: how to add the first bookmark, and a way to check again. */
@Composable
fun NoContentView(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
) {
    EmptyLibraryMessage(
        modifier = modifier,
        icon = Icons.Outlined.BookmarkBorder,
        title = "No bookmarks yet",
        body = "Share a link to Shiori from any app, or add one with the + button below.",
        actionIcon = Icons.Outlined.Refresh,
        actionLabel = "Refresh",
        onAction = onRefresh,
    )
}

/**
 * A search or a filter that found nothing. It used to show the same "No bookmarks yet" as an empty
 * library, under a header that said how many bookmarks there were: two answers to one question.
 */
@Composable
fun NoMatchesView(
    modifier: Modifier = Modifier,
    onClearFilters: () -> Unit,
) {
    EmptyLibraryMessage(
        modifier = modifier,
        icon = Icons.Outlined.SearchOff,
        title = "Nothing matches",
        body = "Try another search, or clear the filters to see the whole library.",
        actionIcon = Icons.Outlined.FilterAltOff,
        actionLabel = "Clear filters",
        onAction = onClearFilters,
    )
}

@Composable
private fun EmptyLibraryMessage(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    body: String,
    actionIcon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyIllustration(icon = icon)
        Text(
            modifier = Modifier.padding(top = 32.dp),
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 10.dp, start = 8.dp, end = 8.dp),
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(
            onClick = onAction,
            modifier = Modifier
                .padding(top = 24.dp)
                .height(48.dp),
            shape = CircleShape,
        ) {
            Icon(actionIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = actionLabel, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * The redesign's shapes in a small heap: the mark on a teal cookie, with a coral flower, a gold sun
 * and an indigo dot around it. Decoration, so screen readers skip it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyIllustration(icon: ImageVector) {
    val scheme = MaterialTheme.colorScheme
    val gold = LocalExtendedColors.current.goldContainer
    Box(modifier = Modifier.size(width = 260.dp, height = 220.dp)) {
        Box(
            modifier = Modifier
                .offset(x = 30.dp, y = 20.dp)
                .size(180.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(scheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(72.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(76.dp)
                .clip(MaterialShapes.Flower.toShape())
                .background(scheme.tertiaryContainer),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-22).dp, y = (-6).dp)
                .size(52.dp)
                .clip(MaterialShapes.Sunny.toShape())
                .background(gold),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 6.dp, y = (-26).dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(scheme.secondary),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 640)
@Composable
private fun NoContentViewPreview() {
    ShioriTheme {
        NoContentView(onRefresh = {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 640)
@Composable
private fun NoMatchesViewPreview() {
    ShioriTheme {
        NoMatchesView(onClearFilters = {})
    }
}
