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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.desarrollodroide.data.extensions.isTimestampId
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.R

@Composable
fun ButtonsView(
    getBookmark: GetBookmark,
    actions: BookmarkActions
) {
    val bookmark by remember { derivedStateOf(getBookmark) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = { actions.onClickEdit(getBookmark) }) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        IconButton(onClick = { actions.onClickDelete(getBookmark) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        if (bookmark.hasEbook) {
            IconButton(onClick = { actions.onClickEpub(getBookmark) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_book),
                    contentDescription = "Epub",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        IconButton(onClick = { actions.onClickShare(getBookmark) }) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        if (!bookmark.id.isTimestampId()){
            IconButton(onClick = { actions.onClickSync(getBookmark) }) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}