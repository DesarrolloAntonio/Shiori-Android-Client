package com.shiori.androidclient.ui.savein

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shiori.androidclient.ui.components.Categories
import com.shiori.androidclient.ui.components.CategoriesType
import com.shiori.model.Tag

@Composable
fun BookmarkEditorView(
    newTag: MutableState<String>,
    assignedTags: MutableState<List<Tag>>,
    availableTags: State<List<Tag>>,
    saveBookmark: (String) -> Unit,
    url: String
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
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
                    if (!assignedTags.value.map { it.name }.contains(newTag.value)) {
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
                saveBookmark(url)
            }
        ) {
            Text("Save")
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
        assignedTags = assignedTags,
        url = "https://www.google.com",
        saveBookmark = {},
        availableTags = assignedTags,
        newTag = newTag,
    )
}