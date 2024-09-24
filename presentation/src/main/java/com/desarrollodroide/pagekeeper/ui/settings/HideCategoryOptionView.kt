package com.desarrollodroide.pagekeeper.ui.settings

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
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.Categories
import com.desarrollodroide.pagekeeper.ui.components.CategoriesType

@Composable
fun HideCategoryOptionView(
    onApply: (Tag?) -> Unit,
    uniqueCategories: List<Tag>,
    hideTag: Tag?
) {
    var selectedTag by remember { mutableStateOf(hideTag) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select category to hide", style = MaterialTheme.typography.headlineSmall)
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
                selectedTags = listOfNotNull(selectedTag),
                onCategorySelected = { tag ->
                    selectedTag = tag
                },
                onCategoryDeselected = {
                    selectedTag = null
                },
                singleSelection = true
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    selectedTag = null
                    onApply(null)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("None")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onApply(selectedTag)
                },
                modifier = Modifier.weight(1f),
                enabled = uniqueCategories.isNotEmpty()
            ) {
                Text("Apply")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SortAndFilterScreenPreview() {
    val regionOptions =
            listOf(
                Tag(id = 1, name = "Northern Europe"), Tag(id = 2, name = "Western Europe"),
                Tag(id = 3, name = "Southern Europe"), Tag(id = 4, name = "Southeast Europe"),
                Tag(id = 5, name = "Central Europe"), Tag(id = 6, name = "Eastern Europe")
            )

    MaterialTheme {
        HideCategoryOptionView(
            onApply = {},
            uniqueCategories = regionOptions,
            hideTag = null
        )
    }
}