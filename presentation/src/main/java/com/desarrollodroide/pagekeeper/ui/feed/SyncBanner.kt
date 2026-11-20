package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.pagekeeper.ui.theme.keyedColors

/** What the banner needs to say: how much is queued, how much of it failed, and whose it is. */
@Immutable
data class SyncBannerState(
    val pending: Int,
    val failed: Int,
    val titles: List<String>,
    val onClick: () -> Unit,
)

/** Built from the jobs WorkManager reports. Null when nothing is waiting, so no banner shows. */
internal fun syncBannerStateOf(jobs: List<PendingJob>, onClick: () -> Unit): SyncBannerState? =
    if (jobs.isEmpty()) null else SyncBannerState(
        pending = jobs.size,
        failed = jobs.count { it.state.uppercase() == "FAILED" },
        titles = jobs.map { it.bookmarkTitle },
        onClick = onClick,
    )

/** Provided by the library route and read by the list, which draws the banner as its first row. */
internal val LocalSyncBanner = compositionLocalOf<SyncBannerState?> { null }

/**
 * "3 waiting to sync", as a dark bar at the top of the list, only while something is queued.
 *
 * It used to be a badge on a toolbar button and nothing else, so a failed change could sit in the
 * queue unnoticed. The bar names the count and the failures and opens the queue, and because it is
 * a list row it scrolls away with the list instead of costing space while reading.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SyncBanner(state: SyncBannerState, modifier: Modifier = Modifier) {
    val shapes = listOf(MaterialShapes.Cookie9Sided, MaterialShapes.Clover4Leaf, MaterialShapes.Sunny)
    Surface(
        onClick = state.onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // A small stack of the queued bookmarks' monograms, so the bar reads as "these".
            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                state.titles.take(3).forEachIndexed { index, title ->
                    val colors = keyedColors(title)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(shapes[index % shapes.size].toShape())
                            .background(colors.container),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = title.trim().take(1).ifEmpty { "?" },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = colors.content,
                        )
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${state.pending} waiting to sync",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (state.failed > 0) "${state.failed} failed · tap to see the queue" else "Tap to see the queue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.78f),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SyncBannerPreview() {
    ShioriTheme {
        SyncBanner(
            state = SyncBannerState(
                pending = 3,
                failed = 1,
                titles = listOf("github.com/go-shiori/shiori", "How SQLite Is Tested", "Effective Go"),
                onClick = { },
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
