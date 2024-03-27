package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    bookmarkEditorType: BookmarkEditorType,
    newTag: MutableState<String>,
    assignedTags: MutableState<List<Tag>>,
    availableTags: State<List<Tag>>,
    saveBookmark: (BookmarkEditorType) -> Unit,
    onBackClick: () -> Unit,
    createArchive: MutableState<Boolean>,
    makeArchivePublic: MutableState<Boolean>,
    createEbook: MutableState<Boolean>,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(CenterVertically)
            )
            Spacer(modifier = Modifier.width(56.dp))
        }

        Row {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = newTag.value,
                onValueChange = { newTag.value = it },
                label = { Text("Add Tag") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Label, contentDescription = "Tag")
                }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                modifier = Modifier
                    .align(CenterVertically),
                onClick = {
                    if (newTag.value.isNotBlank() && !assignedTags.value.map { it.name }.contains(newTag.value)) {
                        assignedTags.value = assignedTags.value + Tag(newTag.value)
                        newTag.value = ""
                    }
                }
            ) {
                Text(text = "Add")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Categories(
            categoriesType = CategoriesType.REMOVEABLES,
            showCategories = true,
            uniqueCategories = assignedTags,
            onCategoriesSelectedChanged = {}
        )
        Divider(modifier = Modifier.padding(vertical = 10.dp))
        Text(text = "All Tags")
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
        if (bookmarkEditorType == BookmarkEditorType.ADD) {
            Row(verticalAlignment = CenterVertically) {
                Checkbox(
                    checked = createArchive.value,
                    onCheckedChange = { createArchive.value = it }
                )
                Text("Create archive")
            }
        }
        Row(verticalAlignment = CenterVertically) {
            Checkbox(
                checked = createEbook.value,
                onCheckedChange = { createEbook.value = it }
            )
            Text("Create Ebook")
        }
        Row(verticalAlignment = CenterVertically) {
            Checkbox(
                checked = makeArchivePublic.value,
                onCheckedChange = { makeArchivePublic.value = it }
            )
            Text("Make bookmark publicly available")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier.align(CenterHorizontally),
            onClick = {
                saveBookmark(bookmarkEditorType)
            }
        ) {
            Text("Save Bookmark")
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
        bookmarkEditorType = BookmarkEditorType.ADD,
        assignedTags = assignedTags,
        saveBookmark = {},
        availableTags = assignedTags,
        newTag = newTag,
        onBackClick = {},
        makeArchivePublic = remember { mutableStateOf(true) },
        createArchive = remember { mutableStateOf(false) },
        createEbook = remember { mutableStateOf(false) }
    )
}