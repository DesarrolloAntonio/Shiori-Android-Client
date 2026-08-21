package com.desarrollodroide.pagekeeper.ui.feed.item

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
    val onClickSync: (GetBookmark) -> Unit
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
    viewType: BookmarkViewType
) {
    Card(
        onClick = { actions.onClickBookmark(getBookmark) },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        when (viewType) {
            BookmarkViewType.FULL -> FullBookmarkView(
                getBookmark = getBookmark,
                serverURL = serverURL,
                xSessionId = xSessionId,
                token = token,
                actions = actions
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
