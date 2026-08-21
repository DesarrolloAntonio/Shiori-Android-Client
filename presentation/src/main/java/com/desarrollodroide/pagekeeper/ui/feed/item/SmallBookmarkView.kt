package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.isTimestampId
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.extensions.isRTLText

private val ThumbnailSize = 72.dp

@Composable
fun SmallBookmarkView(
    getBookmark: GetBookmark,
    serverURL: String,
    xSessionId: String,
    token: String,
    actions: BookmarkActions
) {
    val bookmark by remember { derivedStateOf(getBookmark) }
    val imageUrl by remember {
        derivedStateOf { "${serverURL.removeTrailingSlash()}${bookmark.imageURL}" }
    }
    val isRtl by remember {
        derivedStateOf { bookmark.title.isRTLText() || bookmark.excerpt.isRTLText() }
    }

    Column {
        if (bookmark.isPendingServerProcessing) {
            PendingSyncBanner()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (bookmark.imageURL.isNotEmpty()) {
                BookmarkImageView(
                    imageUrl = imageUrl,
                    xSessionId = xSessionId,
                    token = token,
                    modifier = Modifier
                        .size(ThumbnailSize)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop,
                    loadAsThumbnail = true
                )
                Spacer(modifier = Modifier.size(12.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    Text(
                        text = bookmark.title.ifEmpty { bookmark.url },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                    Text(
                        text = bookmark.modified,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            BookmarkOverflowMenu(
                getBookmark = getBookmark,
                actions = actions,
                hasEbook = bookmark.hasEbook,
                canSync = !bookmark.id.isTimestampId(),
            )
        }
    }
}

@Composable
private fun BookmarkOverflowMenu(
    getBookmark: GetBookmark,
    actions: BookmarkActions,
    hasEbook: Boolean,
    canSync: Boolean,
) {
    val expanded = remember { mutableStateOf(false) }
    Column {
        IconButton(onClick = { expanded.value = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More actions",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            modifier = Modifier.align(alignment = Alignment.End),
            offset = DpOffset(8.dp, 0.dp),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    expanded.value = false
                    actions.onClickEdit(getBookmark)
                },
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    expanded.value = false
                    actions.onClickDelete(getBookmark)
                },
                leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
            )
            if (hasEbook) {
                DropdownMenuItem(
                    text = { Text("Epub") },
                    onClick = {
                        expanded.value = false
                        actions.onClickEpub(getBookmark)
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.ic_book), contentDescription = null)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("Share") },
                onClick = {
                    expanded.value = false
                    actions.onClickShare(getBookmark)
                },
                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) }
            )
            if (canSync) {
                DropdownMenuItem(
                    text = { Text("Update") },
                    onClick = {
                        expanded.value = false
                        actions.onClickSync(getBookmark)
                    },
                    leadingIcon = { Icon(Icons.Outlined.CloudUpload, contentDescription = null) }
                )
            }
        }
    }
}
