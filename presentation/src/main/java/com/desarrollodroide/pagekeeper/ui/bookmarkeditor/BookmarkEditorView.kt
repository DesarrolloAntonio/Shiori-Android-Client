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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
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
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
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
        )
        Divider(modifier = Modifier.padding(vertical = 10.dp))
        Text(text = "All Tags")
        TagsSelectorView(
            availableTags = availableTags.value,
            onTagSelected = {
                if (!assignedTags.value.contains(it)) {
                    assignedTags.value = assignedTags.value + it
                }
            }
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
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
        selected = true
    )
    val tag2 = Tag(
        id = 2,
        name = "tag2",
        selected = false
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
    )
}