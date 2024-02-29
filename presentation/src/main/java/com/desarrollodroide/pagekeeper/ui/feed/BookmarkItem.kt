package com.desarrollodroide.pagekeeper.ui.feed

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.desarrollodroide.data.extensions.removeTrailingSlash
import com.desarrollodroide.model.Bookmark
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.R
import okhttp3.Headers

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    serverURL: String,
    xSessionId: String,
    onClickEdit: (Bookmark) -> Unit,
    onClickDelete: (Bookmark) -> Unit,
    onClickShare: (Bookmark) -> Unit,
    onClickCategory: (Tag) -> Unit,
    onClickBookmark: (Bookmark) -> Unit,
    onClickEpub: (Bookmark) -> Unit,
    onClickSync: (Bookmark) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClickBookmark(bookmark) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentScale = ContentScale.FillWidth,
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${serverURL.removeTrailingSlash()}${bookmark.imageURL}")
                    .headers(
                        Headers.Builder()
                            .add("X-Session-Id", xSessionId)
                            .build()
                    )
                    .build(),
                contentDescription = "ImageRequest example",
                loading = { },
                onError = { },
            )
            Column(
                modifier =
                Modifier
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    text = bookmark.title,
                    fontSize = 24.sp,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    color = MaterialTheme.colorScheme.secondary,
                    text = bookmark.modified,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall

                )
                Text(
                    color = MaterialTheme.colorScheme.secondary,
                    text = bookmark.excerpt,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(5.dp))
                ClickableCategoriesView(
                    uniqueCategories = bookmark.tags,
                    onClickCategory = onClickCategory
                )
                ButtonsView(
                    bookmark = bookmark,
                    onclickDelete = { onClickDelete(bookmark) },
                    onClickEdit = { onClickEdit(bookmark) },
                    onClickShare = { onClickShare(bookmark) },
                    onClickEpub = { onClickEpub(bookmark) },
                    onClickSync = { onClickSync(bookmark) }
                )
            }
        }
    }
}

@Composable
fun ButtonsView(
    bookmark: Bookmark,
    onClickEdit: () -> Unit,
    onclickDelete: () -> Unit,
    onClickShare: () -> Unit,
    onClickEpub: () -> Unit,
    onClickSync: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onClickEdit) {
            Icon(
                tint = MaterialTheme.colorScheme.secondary,
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit"
            )
        }

        IconButton(onClick = onclickDelete) {
            Icon(
                tint = MaterialTheme.colorScheme.secondary,
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete"
            )
        }
        if (bookmark.hasEbook){
            IconButton(onClick = onClickEpub) {
                Icon(
                    tint = MaterialTheme.colorScheme.secondary,
                    painter = painterResource(id = R.drawable.ic_book),
                    contentDescription = "Epub"
                )
            }
        }

        IconButton(onClick = onClickShare) {
            Icon(
                tint = MaterialTheme.colorScheme.secondary,
                imageVector = Icons.Filled.Share,
                contentDescription = "Share"
            )
        }

        IconButton(onClick = onClickSync) {
            Icon(
                tint = MaterialTheme.colorScheme.secondary,
                imageVector = Icons.Filled.CloudUpload,
                contentDescription = "Sync"
            )
        }
    }
}


@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ClickableCategoriesView(
    uniqueCategories: List<Tag>,
    onClickCategory: (Tag) -> Unit
) {
    FlowRow(
    ) {
        uniqueCategories.forEach { category ->
            Text(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .clickable { onClickCategory(category) }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = category.name
            )
        }
    }
}


@Preview
@Composable
fun PreviewPost() {
    MaterialTheme() {
        val bookmark = Bookmark(
            id = -1,
            url= "url",
            title = "Bookmark title",
            excerpt = "Bookmark content",
            author = "",
            public = 1,
            modified = "",
            imageURL = "",
            hasContent = true,
            hasArchive = true,
            hasEbook = false,
            createArchive = true,
            createEbook = true,
            tags = listOf(Tag("tag1"), Tag("tag2")),
        )
        BookmarkItem(
            bookmark = bookmark,
            serverURL = "",
            xSessionId = "",
            onClickEdit = {},
            onClickDelete = {},
            onClickShare = {},
            onClickCategory = {},
            onClickBookmark = {},
            onClickEpub = {},
            onClickSync = {}
        )
    }
}