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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
    uniqueCategories: List<Tag>,
    selectedTags: List<Tag>,
    onCategorySelected: (Tag) -> Unit,
    onCategoryDeselected: (Tag) -> Unit,
    singleSelection: Boolean = false
) {
    AnimatedVisibility(showCategories) {
        Column {
            FlowRow {
                uniqueCategories.forEach { category ->
                    val selected = selectedTags.any { it.name == category.name }
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
                        label = { Text(category.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            when (categoriesType) {
                                CategoriesType.SELECTABLES -> {
                                    if (singleSelection) {
                                        selectedTags.forEach {
                                            onCategoryDeselected(it)
                                        }
                                        if (!selected) {
                                            onCategorySelected(category)
                                        }
                                    } else {
                                        if (selected) {
                                            onCategoryDeselected(category)
                                        } else {
                                            onCategorySelected(category)
                                        }
                                    }
                                }
                                CategoriesType.REMOVEABLES -> {
                                    onCategoryDeselected(category)
                                }
                            }
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
