package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoriesView(
    onApply: (List<Tag>) -> Unit,
    onDismiss: () -> Unit,
    uniqueCategories: MutableState<List<Tag>>,
) {
    val context = LocalContext.current
    val sortingOptions = remember { mutableStateListOf(Tag("Alphabetical order"), Tag("Date")) }
    val selectedSorting = remember { mutableStateOf(listOf<Tag>()) }
    val mutableUniqueCategories = remember { mutableStateOf(uniqueCategories) }

    val selectedTags = remember { mutableStateOf(listOf<Tag>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Text("Sort by", style = MaterialTheme.typography.headlineSmall)
//        Categories(
//            categoriesType = CategoriesType.SELECTABLES,
//            showCategories = true,
//            uniqueCategories = remember { mutableStateOf(sortingOptions) },
//            selectedTags = selectedSorting,
//            onCategoriesSelectedChanged = { tags ->
//                selectedSorting.value = tags.take(1) // Only allow one sorting option
//            }
//        )

//        Spacer(Modifier.height(24.dp))

        Text("Categories", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Categories(
            categoriesType = CategoriesType.SELECTABLES,
            showCategories = true,
            uniqueCategories = uniqueCategories,
            selectedTags = selectedTags,
            onCategoriesSelectedChanged = { tags ->
                selectedTags.value = tags
            }
        )

        Spacer(Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    selectedTags.value = listOf()
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
    )}
    MaterialTheme {
        CategoriesView(
            onApply = {},
            onDismiss = {},
            uniqueCategories = regionOptions
        )
    }
}