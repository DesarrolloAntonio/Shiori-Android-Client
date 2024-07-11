package com.desarrollodroide.pagekeeper.ui.components

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag

enum class CategoriesType {
    SELECTABLES, REMOVEABLES
}
@Composable
@OptIn(ExperimentalLayoutApi::class)
fun Categories(
    categoriesType: CategoriesType = CategoriesType.SELECTABLES,
    showCategories: Boolean,
    uniqueCategories: MutableState<List<Tag>>,
    selectedTags: MutableState<List<Tag>> = mutableStateOf(emptyList<Tag>()),
    onCategoriesSelectedChanged: (List<Tag>) -> Unit,
    singleSelection: Boolean = false
) {
    Log.v("selectedTags", "selectedTags: $selectedTags")
    AnimatedVisibility(showCategories) {
        Column {
            FlowRow {
                uniqueCategories.value.forEach { category ->
                    val selected = selectedTags.value.any { it.id == category.id }
                    FilterChip(
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            iconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            disabledSelectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        selected = selected,
                        label = { Text(category.name) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            when (categoriesType) {
                                CategoriesType.SELECTABLES -> {
                                    if (singleSelection) {
                                        selectedTags.value = if (selected) emptyList() else listOf(category)
                                    } else {
                                        if (selected) {
                                            selectedTags.value = selectedTags.value - category
                                        } else {
                                            selectedTags.value = selectedTags.value + category
                                        }
                                    }
                                }
                                CategoriesType.REMOVEABLES -> {
                                    uniqueCategories.value = uniqueCategories.value.filter { it != category }
                                }
                            }
                            onCategoriesSelectedChanged(selectedTags.value)
                        },
                        leadingIcon = {
                            when(categoriesType){
                                CategoriesType.SELECTABLES -> {
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Done,
                                            contentDescription = null,
                                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                                        )
                                    }
                                }
                                CategoriesType.REMOVEABLES -> {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
