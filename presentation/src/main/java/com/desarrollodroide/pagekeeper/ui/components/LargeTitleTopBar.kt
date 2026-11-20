package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The header of the app's plainer screens — the legal pages and the debug logs: Back, the screen's
 * own actions, and its name set large under them. The large name shrinks away as the content
 * scrolls and a small one takes its place beside Back, the way the Settings header does.
 *
 * They used to have a centred small-title app bar, the only screens left in the old style.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LargeTitleTopBar(
    title: String,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val state = scrollBehavior.state
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { LargeTitleHeight.toPx() }
    SideEffect {
        if (state.heightOffsetLimit != -expandedHeightPx) {
            state.heightOffsetLimit = -expandedHeightPx
        }
    }
    val collapsedFraction = state.collapsedFraction
    val titleHeight = with(density) { (expandedHeightPx + state.heightOffset).coerceAtLeast(0f).toDp() }

    Surface(color = lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer, collapsedFraction)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .graphicsLayer { alpha = collapsedFraction },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                actions()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(titleHeight)
                    .clipToBounds(),
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                        .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                        .graphicsLayer { alpha = 1f - collapsedFraction }
                        .semantics { heading() },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 46.sp,
                        lineHeight = 50.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.8).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

private val LargeTitleHeight = 62.dp
