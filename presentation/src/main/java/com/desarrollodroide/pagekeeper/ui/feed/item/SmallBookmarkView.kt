package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.pagekeeper.R

@Composable
fun SmallBookmarkView(
    bookmark: Bookmark,
    serverURL: String,
    xSessionId: String,
    isLegacyApi: Boolean,
    token: String,
    actions: BookmarkActions
) {
    val imageUrl =
        "${serverURL.removeTrailingSlash()}${bookmark.imageURL}?lastUpdated=${bookmark.modified}"
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(start = 8.dp)
            .height(75.dp)
    ) {
        BookmarkImageView(
            imageUrl = imageUrl,
            xSessionId = xSessionId,
            isLegacyApi = isLegacyApi,
            token = token,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(
                    RoundedCornerShape(8.dp)
                ),
            contentScale = ContentScale.Crop,
            loadAsThumbnail = true
        )
        Spacer(modifier = Modifier.width(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = bookmark.title,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bookmark.modified,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
            Column {
                val expanded = remember { mutableStateOf(false) }
                IconButton(onClick = {
                    expanded.value = true
                }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                DropdownMenu(
                    modifier = Modifier
                        .align(alignment = Alignment.End),
                    offset = DpOffset((8).dp, 0.dp),
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded.value = false
                            actions.onClickEdit(bookmark)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null
                            )
                        })
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded.value = false
                            actions.onClickDelete(bookmark)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null
                            )
                        })
                    if (bookmark.hasEbook){
                        DropdownMenuItem(
                            text = { Text("Epub") },
                            onClick = {
                                expanded.value = false
                                actions.onClickEpub(bookmark)
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_book),
                                    contentDescription = "Epub",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            })
                    }
                    DropdownMenuItem(
                        text = { Text("Share") },
                        onClick = {
                            expanded.value = false
                            actions.onClickShare(bookmark)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Share,
                                contentDescription = null
                            )
                        })
                    DropdownMenuItem(
                        text = { Text("Update") },
                        onClick = {
                            expanded.value = false
                            actions.onClickSync(bookmark)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.CloudUpload,
                                contentDescription = null
                            )
                        })
                }
            }
        }
    }
}