package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

data class BookmarkActions(
    val onClickEdit: (GetBookmark) -> Unit,
    val onClickDelete: (GetBookmark) -> Unit,
    val onClickShare: (GetBookmark) -> Unit,
    val onClickCategory: (Tag) -> Unit,
    val onClickBookmark: (GetBookmark) -> Unit,
    val onClickEpub: (GetBookmark) -> Unit,
    val onClickSync: (GetBookmark) -> Unit,
    val onToggleSelection: (Int) -> Unit = {},
    /** Re-fetch just this bookmark; what the pending banner offers. */
    val onClickRefresh: (GetBookmark) -> Unit = {},
)

typealias GetBookmark = () -> Bookmark

/**
 * One bookmark in the feed, as a tonal card.
 *
 * The feed used to be a flat run of rows separated by hairline dividers. Cards on
 * `surfaceContainerLow` give each bookmark its own volume, which is how M3 expresses grouping —
 * so the dividers are gone and the separation comes from the surface tone plus the list spacing.
 */
@Composable
fun BookmarkItem(
    getBookmark: GetBookmark,
    serverURL: String,
    xSessionId: String,
    token: String,
    actions: BookmarkActions,
    viewType: BookmarkViewType,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    isRefreshing: Boolean = false,
    hasSettledEmpty: Boolean = false,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Long press starts batch edit. Once it is running a plain tap picks bookmarks instead
            // of opening them, otherwise selecting a second one would navigate away from the list.
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        actions.onToggleSelection(getBookmark().id)
                    } else {
                        actions.onClickBookmark(getBookmark)
                    }
                },
                onLongClick = { actions.onToggleSelection(getBookmark().id) },
            )
            .semantics { selected = isSelected },
        // The full card is a large object with a cover inside it, so it takes the roundest corner;
        // the compact row keeps the large one, like a list item.
        shape = when (viewType) {
            BookmarkViewType.FULL -> MaterialTheme.shapes.extraLarge
            BookmarkViewType.SMALL -> MaterialTheme.shapes.large
        },
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            // A light wash of the brand colour, not a full container: tag chips use the
            // containers, and on a fully tinted card they lost their pill and read as loose text.
            containerColor = if (isSelected) {
                lerp(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.primaryContainer, 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
      Box {
        when (viewType) {
            BookmarkViewType.FULL -> FullBookmarkView(
                getBookmark = getBookmark,
                serverURL = serverURL,
                xSessionId = xSessionId,
                token = token,
                actions = actions,
                isRefreshing = isRefreshing,
                hasSettledEmpty = hasSettledEmpty,
            )

            BookmarkViewType.SMALL -> SmallBookmarkView(
                getBookmark = getBookmark,
                serverURL = serverURL,
                xSessionId = xSessionId,
                token = token,
                actions = actions
            )
        }
        // While picking, every card carries a check on its picture's corner: filled when picked,
        // an empty ring when not, so which ones are in is readable down the whole list.
        // Qualified: inside the card's ColumnScope the bare name resolves to the column overload.
        androidx.compose.animation.AnimatedVisibility(
            visible = isSelectionMode,
            // Inside the card's rounded corner, on the picture, rather than clipped by the corner.
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            SelectionCheck(isSelected = isSelected)
        }
      }
    }
}

@Composable
private fun SelectionCheck(isSelected: Boolean) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isSelected) scheme.primary else scheme.surfaceContainerLowest)
            .border(
                width = if (isSelected) 0.dp else 2.dp,
                color = if (isSelected) Color.Transparent else scheme.outline,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = scheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview
@Composable
private fun BookmarkItemPreview() {
    ShioriTheme {
        val mockBookmark = Bookmark.mock()
        val actions = BookmarkActions(
            onClickEdit = { },
            onClickDelete = { },
            onClickShare = { },
            onClickCategory = { },
            onClickBookmark = { },
            onClickEpub = { },
            onClickSync = { }
        )
        Column {
            BookmarkItem(
                getBookmark = { mockBookmark },
                serverURL = "",
                xSessionId = "",
                token = "",
                actions = actions,
                viewType = BookmarkViewType.FULL
            )
            BookmarkItem(
                getBookmark = { mockBookmark },
                serverURL = "",
                xSessionId = "",
                token = "",
                actions = actions,
                viewType = BookmarkViewType.SMALL
            )
        }
    }
}
