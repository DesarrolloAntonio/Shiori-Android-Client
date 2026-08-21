package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
 * This used the Expressive ButtonGroup, which crashes when measured inside a LazyColumn:
 * ButtonGroupMeasurePolicy builds constraints with maxWidth below minWidth and throws, taking the
 * whole feed down on the first frame. It is not a usage problem, removing the custom arrangement
 * changes nothing, so it is a defect in material3 1.5.0-alpha18. See BookmarkItemLayoutTest, which
 * fails against ButtonGroup and passes against this.
 *
 * Tonal icon buttons keep the expressive look without the alpha component.
 */
@Composable
fun ButtonsView(
    getBookmark: GetBookmark,
    actions: BookmarkActions,
    modifier: Modifier = Modifier,
) {
    val bookmark by remember { derivedStateOf(getBookmark) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { actions.onClickEdit(getBookmark) },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = "Edit")
        }
        IconButton(
            onClick = { actions.onClickShare(getBookmark) },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        ) {
            Icon(Icons.Outlined.Share, contentDescription = "Share")
        }
        if (bookmark.hasEbook) {
            IconButton(
                onClick = { actions.onClickEpub(getBookmark) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
                Icon(painterResource(id = R.drawable.ic_book), contentDescription = "Epub")
            }
        }
        if (!bookmark.id.isTimestampId()) {
            IconButton(
                onClick = { actions.onClickSync(getBookmark) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = "Update")
            }
        }
        IconButton(
            onClick = { actions.onClickDelete(getBookmark) },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
        ) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
        }
    }
}
