package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.Categories
import com.desarrollodroide.pagekeeper.ui.components.CategoriesType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesView(
    onDismiss: () -> Unit,
    uniqueCategories: List<Tag>,
    tagToHide: Tag?,
    onFilterHiddenTag: (Boolean) -> Unit,
    selectedOptionIndex: Int,
    onSelectedOptionIndexChanged: (Int) -> Unit,
    selectedTags: List<Tag>,
    onCategorySelected: (Tag) -> Unit,
    onCategoryDeselected: (Tag) -> Unit,
    onResetAll: () -> Unit
) {
//    val filteredCategories = remember(uniqueCategories, tagToHide) {
//        uniqueCategories.filter { it.name != tagToHide?.name }
//    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = tagToHide != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = "Hidden tag",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hidden: ${tagToHide?.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Bookmarks with this tag are currently hidden.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedOptionIndex == 0,
                        onClick = {
                            onSelectedOptionIndexChanged(0)
                            onFilterHiddenTag(false)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) {
                        Text("All", style = MaterialTheme.typography.titleMedium)
                    }
                    SegmentedButton(
                        selected = selectedOptionIndex == 1,
                        onClick = {
                            onSelectedOptionIndexChanged(1)
                            onFilterHiddenTag(true)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) {
                        Text("Hidden tag", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = if (selectedOptionIndex == 0)
                        "Filter by all bookmarks\n"
                    else
                        "Showing only bookmarks with the '${tagToHide?.name}' tag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = selectedOptionIndex == 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Categories", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                if (uniqueCategories.isEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Sell,
                            contentDescription = "No categories available",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "No categories available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Categories(
                        categoriesType = CategoriesType.SELECTABLES,
                        showCategories = true,
                        uniqueCategories = uniqueCategories,
                        selectedTags = selectedTags,
                        onCategorySelected = { tag ->
                            onCategorySelected(tag)
                            onFilterHiddenTag(false)
                        },
                        onCategoryDeselected = { tag ->
                            onCategoryDeselected(tag)
                            onFilterHiddenTag(false)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                enabled = selectedOptionIndex == 0,
                onClick = {
                    //onUpdateSelectedTags(emptyList())
                    onResetAll()
                    onFilterHiddenTag(false)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset All")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Close")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SortAndFilterScreenPreview() {
    val regionOptions = listOf(
        Tag(id = 1, name = "Northern Europe"),
        Tag(id = 2, name = "Western Europe"),
        Tag(id = 3, name = "Southern Europe"),
        Tag(id = 4, name = "Southeast Europe"),
        Tag(id = 5, name = "Central Europe"),
        Tag(id = 6, name = "Eastern Europe")
    )

    val selectedOptionIndex = remember { mutableStateOf(0) }
    val selectedTags = remember { mutableStateOf(listOf(Tag(id = 3, name = "Southern Europe"))) }

    MaterialTheme {
        CategoriesView(
            onDismiss = {},
            uniqueCategories = regionOptions,
            tagToHide = Tag(id = 3, name = "Southeast Europe"),
            onFilterHiddenTag = {},
            selectedOptionIndex = selectedOptionIndex.value,
            onSelectedOptionIndexChanged = { selectedOptionIndex.value = it },
            selectedTags = selectedTags.value,
            onCategorySelected = { },
            onCategoryDeselected = { },
            onResetAll = { }
        )
    }
}
