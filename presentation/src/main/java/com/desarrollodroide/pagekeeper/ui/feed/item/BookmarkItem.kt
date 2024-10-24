package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.helpers.BookmarkViewType
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag

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

@Composable
fun BookmarkItem(
    getBookmark: GetBookmark,
    serverURL: String,
    xSessionId: String,
    token: String,
    actions: BookmarkActions,
    viewType: BookmarkViewType
) {
    val bookmark by remember { derivedStateOf(getBookmark) }
    Box(modifier = Modifier
        .padding(horizontal = 6.dp)
        .padding(bottom = if (viewType == BookmarkViewType.FULL) 0.dp else 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { actions.onClickBookmark(getBookmark) },
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
}

@Preview
@Composable
fun PreviewPost() {
    MaterialTheme {
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