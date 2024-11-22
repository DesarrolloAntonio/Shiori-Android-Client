package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DefaultsSection(
    makeArchivePublic: Boolean,
    onMakeArchivePublicChanged: (Boolean) -> Unit,
    createEbook: Boolean,
    onCreateEbookChanged: (Boolean) -> Unit,
    createArchive: Boolean,
    onCreateArchiveChanged: (Boolean) -> Unit,
    autoAddBookmark: Boolean,
    onAutoAddBookmarkChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 5.dp)
    ) {
        Text(text = "Defaults", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(5.dp))
        SwitchOption(
            title = "Make bookmark publicly available",
            icon = Icons.Filled.Public,
            checked = makeArchivePublic,
            onCheckedChange = onMakeArchivePublicChanged
        )
        SwitchOption(
            title = "Create archive",
            icon = Icons.Filled.Archive,
            checked = createArchive,
            onCheckedChange = onCreateArchiveChanged
        )
        SwitchOption(
            title = "Create Ebook",
            icon = Icons.Filled.Book,
            checked = createEbook,
            onCheckedChange = onCreateEbookChanged
        )
        SwitchOption(
            title = "Add bookmark automatically",
            icon = Icons.Filled.BookmarkAdd,
            checked = autoAddBookmark,
            onCheckedChange = onAutoAddBookmarkChanged
        )
    }
}