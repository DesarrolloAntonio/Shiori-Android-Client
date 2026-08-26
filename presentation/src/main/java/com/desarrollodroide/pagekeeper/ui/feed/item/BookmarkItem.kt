package com.desarrollodroide.pagekeeper.ui.feed.item

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
        shape = MaterialTheme.shapes.large,
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
        colors = CardDefaults.cardColors(
            // Not secondaryContainer: the tag chips already use it, so on a selected card the
            // chips lost their pill and read as loose text. The border carries the selection.
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.surfaceContainerHighest
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
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
