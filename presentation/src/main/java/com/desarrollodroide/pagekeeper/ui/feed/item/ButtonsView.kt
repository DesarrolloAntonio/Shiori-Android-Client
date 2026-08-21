package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.isTimestampId
import com.desarrollodroide.pagekeeper.R

/**
 * Row of per-bookmark actions.
 *
 * Uses the Expressive [ButtonGroup]: buttons squeeze and stretch against their neighbours as you
 * press them, and anything that doesn't fit collapses into the overflow menu automatically —
 * which matters here because the number of actions varies per bookmark (epub, sync).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ButtonsView(
    getBookmark: GetBookmark,
    actions: BookmarkActions,
) {
    val bookmark by remember { derivedStateOf(getBookmark) }

    ButtonGroup(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        overflowIndicator = { menuState ->
            IconButton(onClick = { if (menuState.isShowing) menuState.dismiss() else menuState.show() }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "More actions")
            }
        },
    ) {
        clickableItem(
            onClick = { actions.onClickEdit(getBookmark) },
            label = "Edit",
            icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        )
        clickableItem(
            onClick = { actions.onClickShare(getBookmark) },
            label = "Share",
            icon = { Icon(Icons.Outlined.Share, contentDescription = null) },
        )
        if (bookmark.hasEbook) {
            clickableItem(
                onClick = { actions.onClickEpub(getBookmark) },
                label = "Epub",
                icon = { Icon(painterResource(id = R.drawable.ic_book), contentDescription = null) },
            )
        }
        if (!bookmark.id.isTimestampId()) {
            clickableItem(
                onClick = { actions.onClickSync(getBookmark) },
                label = "Update",
                icon = { Icon(Icons.Outlined.CloudUpload, contentDescription = null) },
            )
        }
        clickableItem(
            onClick = { actions.onClickDelete(getBookmark) },
            label = "Delete",
            icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
        )
    }
}
