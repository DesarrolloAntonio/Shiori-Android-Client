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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.Categories
import com.desarrollodroide.pagekeeper.ui.components.CategoriesType

@Composable
fun HideCategoryOptionView(
    onApply: (Tag?) -> Unit,
    uniqueCategories: MutableState<List<Tag>>,
) {
    val selectedTags = remember { mutableStateOf(listOf<Tag>()) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select category to hide", style = MaterialTheme.typography.headlineSmall)
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
                uniqueCategories = uniqueCategories,
                selectedTags = selectedTags,
                onCategoriesSelectedChanged = { tags ->
                    selectedTags.value = tags
                },
                singleSelection = true
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    selectedTags.value = listOf()
                    onApply(null)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("None")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onApply(selectedTags.value.firstOrNull())
                },
                modifier = Modifier.weight(1f),
                enabled = uniqueCategories.value.isNotEmpty()
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
        HideCategoryOptionView(
            onApply = {},
            uniqueCategories = regionOptions
        )
    }
}