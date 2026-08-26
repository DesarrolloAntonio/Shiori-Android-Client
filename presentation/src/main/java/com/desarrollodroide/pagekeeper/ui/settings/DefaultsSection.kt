package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    modifier: Modifier = Modifier,
) {
    SettingsGroup(title = "Defaults", modifier = modifier) {
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
