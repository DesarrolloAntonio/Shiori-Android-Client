package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.Categories
import com.desarrollodroide.pagekeeper.ui.components.CategoriesType
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesView(
    onApply: (List<Tag>) -> Unit,
    onDismiss: () -> Unit,
    uniqueCategories: MutableState<List<Tag>>,
    tagToHide: Tag?,
    onFilterHiddenTag: (Boolean) -> Unit,
    selectedOptionIndex: MutableState<Int>,
    selectedTags: MutableState<List<Tag>>
) {
    // Filter out the tagToHide from uniqueCategories
    val filteredCategories = remember(uniqueCategories.value, tagToHide) {
        mutableStateOf(uniqueCategories.value.filter { it.name != tagToHide?.name })
    }

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
                        selected = selectedOptionIndex.value == 0,
                        onClick = {
                            selectedOptionIndex.value = 0
                            onFilterHiddenTag(false)
                            onApply(selectedTags.value)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) {
                        Text("All", style = MaterialTheme.typography.titleMedium)
                    }
                    SegmentedButton(
                        selected = selectedOptionIndex.value == 1,
                        onClick = {
                            selectedOptionIndex.value = 1
                            onFilterHiddenTag(true)
                            onApply(selectedTags.value)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) {
                        Text("Hidden tag", style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = if (selectedOptionIndex.value == 0)
                        "Filter by all bookmarks\n"
                    else
                        "Showing only bookmarks with the '${tagToHide?.name}' tag",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = selectedOptionIndex.value == 0,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Categories", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp))

                if (uniqueCategories.value.isEmpty()) {
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
                        uniqueCategories = filteredCategories,
                        selectedTags = selectedTags,
                        onCategoriesSelectedChanged = { tags ->
                            selectedTags.value = tags
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    selectedTags.value = listOf()
                    onFilterHiddenTag(false)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset All")
            }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    onApply(selectedTags.value)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Apply")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SortAndFilterScreenPreview() {
    val regionOptions = remember {
        mutableStateOf(
            listOf(
                Tag("Northern Europe"), Tag("Western Europe"),
                Tag("Southern Europe"), Tag("Southeast Europe"),
                Tag("Central Europe"), Tag("Eastern Europe")
            )
        )
    }
    MaterialTheme {
        CategoriesView(
            onApply = {},
            onDismiss = {},
            uniqueCategories = regionOptions,
            tagToHide = Tag("Southeast Europe"),
            onFilterHiddenTag = {},
            selectedOptionIndex = remember { mutableStateOf(0) },
            selectedTags = remember { mutableStateOf(listOf<Tag>(Tag("Southern Europe"))) }
        )
    }
}