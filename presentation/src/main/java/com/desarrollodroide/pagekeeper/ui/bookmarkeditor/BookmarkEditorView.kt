package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.pagekeeper.ui.components.Categories
import com.desarrollodroide.pagekeeper.ui.components.CategoriesType
import com.desarrollodroide.model.Tag

enum class BookmarkEditorType { ADD, EDIT }
@Composable
fun BookmarkEditorView(
    title: String,
    url: String,
    bookmarkEditorType: BookmarkEditorType,
    newTag: MutableState<String>,
    assignedTags: MutableState<List<Tag>>,
    availableTags: State<List<Tag>>,
    saveBookmark: (BookmarkEditorType) -> Unit,
    onBackClick: () -> Unit,
    createArchive: Boolean,
    onCreateArchiveChanged: (Boolean) -> Unit,
    makeArchivePublic: Boolean,
    onMakeArchivePublicChanged: (Boolean) -> Unit,
    createEbook: Boolean,
    onCreateEbookChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(CenterVertically)
            )
            IconButton(onClick = {
                saveBookmark(bookmarkEditorType)
            }) {
                Icon(Icons.Outlined.Save, contentDescription = "Save")
            }
        }
        Text(
            text = url,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis //
        )

        if (bookmarkEditorType == BookmarkEditorType.ADD) {
            Row(verticalAlignment = CenterVertically) {
                Checkbox(
                    checked = createArchive,
                    onCheckedChange = onCreateArchiveChanged
                )
                Text("Create archive")
            }
        }
        Row(verticalAlignment = CenterVertically) {
            Checkbox(
                checked = createEbook,
                onCheckedChange = onCreateEbookChanged
            )
            Text("Create Ebook")
        }
        Row(verticalAlignment = CenterVertically) {
            Checkbox(
                checked = makeArchivePublic,
                onCheckedChange = onMakeArchivePublicChanged
            )
            Text("Make bookmark publicly available")
        }

        Row {
            OutlinedTextField(
                modifier = Modifier
                    .weight(1f)
                    .align(CenterVertically),
                value = newTag.value,
                onValueChange = { newTag.value = it },
                label = { Text("Add Tag") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.Label, contentDescription = "Tag")
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                modifier = Modifier
                    .align(CenterVertically)
                    .padding(top = 4.dp),
                onClick = {
                    if (newTag.value.isNotBlank() && !assignedTags.value.map { it.name }.contains(newTag.value)) {
                        assignedTags.value = assignedTags.value + Tag(id = -1, name = newTag.value)
                        newTag.value = ""
                    }
                }
            ) {
                Text(text = "Add")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .heightIn(max = 145.dp)
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
//            Categories(
//                categoriesType = CategoriesType.REMOVEABLES,
//                showCategories = true,
//                uniqueCategories = assignedTags,
//                onCategoriesSelectedChanged = {}
//            )

            Categories(
                categoriesType = CategoriesType.REMOVEABLES,
                showCategories = true,
                uniqueCategories = assignedTags.value,
                selectedTags = assignedTags.value,
                onCategorySelected = { /* No se usa en modo REMOVEABLES */ },
                onCategoryDeselected = { deselectedTag ->
                    assignedTags.value = assignedTags.value.filter { it != deselectedTag }
                }
            )
        }
        Spacer(modifier = Modifier.heightIn(10.dp))
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = "All Tags"
        )
        Spacer(modifier = Modifier.heightIn(5.dp))
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TagsSelectorView(
                availableTags = availableTags.value,
                onTagSelected = {
                    if (!assignedTags.value.contains(it)) {
                        assignedTags.value = assignedTags.value + it
                    }
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TagsSelectorView(
    availableTags: List<Tag>,
    onTagSelected: (Tag) -> Unit
) {
    FlowRow(
    ) {
        availableTags.forEach { category ->
            Text(
                color = Color.DarkGray,
                modifier = Modifier
                    .padding(5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFEAEDED))
                    .clickable { onTagSelected(category) }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = category.name
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BookmarkEditorPreview() {
    val tag1 = Tag(
        id = 1,
        name = "tag1",
        selected = true,
        nBookmarks = 0
    )
    val tag2 = Tag(
        id = 2,
        name = "tag2",
        selected = false,
        nBookmarks = 0
    )
    val assignedTags = remember { mutableStateOf(listOf<Tag>(tag1, tag2)) }
    val newTag = remember { mutableStateOf("")}

    BookmarkEditorView(
        title = "Add",
        url = "http://www.google.com",
        bookmarkEditorType = BookmarkEditorType.ADD,
        assignedTags = remember { mutableStateOf(generateRandomTags(100)) },
        saveBookmark = {},
        availableTags = assignedTags,
        newTag = newTag,
        onBackClick = {},
        makeArchivePublic = true,
        createArchive = false,
        createEbook = false,
        onMakeArchivePublicChanged = {},
        onCreateEbookChanged = {},
        onCreateArchiveChanged = {}
    )

}

private fun generateRandomTagName(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}

private fun generateRandomTags(count: Int): List<Tag> {
    return List(count) {
        Tag(id = count,name = generateRandomTagName(10))
    }
}