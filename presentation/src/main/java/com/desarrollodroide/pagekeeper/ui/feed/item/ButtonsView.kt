package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.R

@Composable
fun ButtonsView(
    bookmark: Bookmark,
    actions: BookmarkActions
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = { actions.onClickEdit(bookmark) }) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        IconButton(onClick = { actions.onClickDelete(bookmark) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        if (bookmark.hasEbook) {
            IconButton(onClick = { actions.onClickEpub(bookmark) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_book),
                    contentDescription = "Epub",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        IconButton(onClick = { actions.onClickShare(bookmark) }) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        IconButton(onClick = { actions.onClickSync(bookmark) }) {
            Icon(
                imageVector = Icons.Filled.CloudUpload,
                contentDescription = "Sync",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}