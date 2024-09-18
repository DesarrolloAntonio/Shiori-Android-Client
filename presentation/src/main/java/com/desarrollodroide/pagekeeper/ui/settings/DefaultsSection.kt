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
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun DefaultsSection(
    makeArchivePublic: Boolean,
    onMakeArchivePublicChanged: (Boolean) -> Unit,
    createEbook: Boolean,
    onCreateEbookChanged: (Boolean) -> Unit,
    createArchive: MutableStateFlow<Boolean>,
    autoAddBookmark: MutableStateFlow<Boolean>,
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
            title = "Create Ebook",
            icon = Icons.Filled.Book,
            checked = createEbook,
            onCheckedChange = onCreateEbookChanged
        )
        val items = listOf(
            //Item("Make bookmark publicly available", icon = Icons.Filled.Public, switchState = makeArchivePublic),
            Item("Create archive", icon = Icons.Filled.Archive, switchState = createArchive),
            //Item("Create Ebook", icon = Icons.Filled.Book, switchState = createEbook),
            Item("Add bookmark automatically", icon = Icons.Filled.BookmarkAdd, switchState = autoAddBookmark)
        )
        items.forEach { item ->
            SwitchOption(
                item = item,
                switchState = item.switchState
            )
        }
    }
}