package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * The sheet that filters the library by tag.
 *
 * It used to be every tag in one wrapping block, in whatever order the server sent them, with no
 * way to look one up: fine for ten tags, a wall for two hundred. Now there is a search field, a
 * choice between most used and alphabetical, each tag's bookmark count, and the tags already
 * applied always come first, so they can be taken off without hunting for them.
 *
 * A tap still applies or removes a tag at once, as before; Done only closes the sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
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
    var query by rememberSaveable { mutableStateOf("") }
    var sortByName by rememberSaveable { mutableStateOf(false) }
    val selectedNames = remember(selectedTags) { selectedTags.map { it.name }.toSet() }
    val visibleTags = remember(uniqueCategories, selectedNames, query, sortByName) {
        filterAndSortTags(uniqueCategories, selectedNames, query, sortByName)
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Filter by tag",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp),
            )

            if (tagToHide != null) {
                HiddenTagCard(
                    tagToHide = tagToHide,
                    selectedOptionIndex = selectedOptionIndex,
                    onSelectedOptionIndexChanged = onSelectedOptionIndexChanged,
                    onFilterHiddenTag = onFilterHiddenTag,
                )
                Spacer(Modifier.height(16.dp))
            }

            if (selectedOptionIndex == 0) {
                if (uniqueCategories.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Outlined.Sell, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("No tags yet", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    TagSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        tagCount = uniqueCategories.size,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (selectedNames.isEmpty()) "All tags" else "${selectedNames.size} applied",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp),
                        )
                        SortToggle(sortByName = sortByName, onChange = { sortByName = it })
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        visibleTags.forEach { tag ->
                            TagFilterChip(
                                tag = tag,
                                selected = tag.name in selectedNames,
                                onClick = {
                                    if (tag.name in selectedNames) onCategoryDeselected(tag) else onCategorySelected(tag)
                                    onFilterHiddenTag(false)
                                },
                            )
                        }
                    }
                    if (visibleTags.isEmpty()) {
                        Text(
                            text = "No tags match “${query.trim()}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp),
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    enabled = selectedOptionIndex == 0 && selectedTags.isNotEmpty(),
                    onClick = {
                        onResetAll()
                        onFilterHiddenTag(false)
                    },
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                ) {
                    Text("Done", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * Most used or A–Z. Two buttons joined into one control, sized to their labels: the segmented
 * button row splits its width evenly and keeps room for a check mark, which cut "Most used" short.
 */
@Composable
internal fun SortToggle(sortByName: Boolean, onChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        SortOption(
            label = "Most used",
            selected = !sortByName,
            shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp, topEnd = 6.dp, bottomEnd = 6.dp),
            onClick = { onChange(false) },
        )
        SortOption(
            label = "A–Z",
            selected = sortByName,
            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp),
            onClick = { onChange(true) },
        )
    }
}

@Composable
private fun SortOption(label: String, selected: Boolean, shape: RoundedCornerShape, onClick: () -> Unit) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        // The chosen one goes fully round, the Expressive way of saying "this one".
        shape = if (selected) RoundedCornerShape(20.dp) else shape,
        color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

@Composable
internal fun TagSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    tagCount: Int,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Search tags" },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search $tagCount tags",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear tag search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun TagFilterChip(
    tag: Tag,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tag.name)
                if (tag.nBookmarks > 0) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tag.nBookmarks.toString(),
                        fontWeight = FontWeight.Normal,
                    )
                }
            }
        },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        },
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = null,
    )
}

/**
 * Shown only when a tag is set as hidden in Settings: switches between the library without it,
 * which is the normal view, and only the bookmarks that carry it.
 */
@Composable
private fun HiddenTagCard(
    tagToHide: Tag,
    selectedOptionIndex: Int,
    onSelectedOptionIndexChanged: (Int) -> Unit,
    onFilterHiddenTag: (Boolean) -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Hidden: ${tagToHide.name}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Bookmarks with this tag are kept out of the library.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedOptionIndex == 0,
                    onClick = {
                        onSelectedOptionIndexChanged(0)
                        onFilterHiddenTag(false)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("All") }
                SegmentedButton(
                    selected = selectedOptionIndex == 1,
                    onClick = {
                        onSelectedOptionIndexChanged(1)
                        onFilterHiddenTag(true)
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Hidden tag") }
            }
        }
    }
}

/**
 * The tags the sheet shows, in the order it shows them.
 *
 * Applied tags come first, whatever the sort, so they are always at hand to be taken off. A search
 * matches anywhere in the name and ignores case, and never hides an applied tag that does not
 * match: that would make it look as if the filter had gone.
 */
internal fun filterAndSortTags(
    tags: List<Tag>,
    selectedNames: Set<String>,
    query: String,
    sortByName: Boolean,
): List<Tag> {
    val needle = query.trim()
    val matching = tags.filter { it.name in selectedNames || needle.isEmpty() || it.name.contains(needle, ignoreCase = true) }
    val byName = compareBy<Tag> { it.name.lowercase() }
    val order = if (sortByName) byName else compareByDescending<Tag> { it.nBookmarks }.then(byName)
    val (applied, rest) = matching.partition { it.name in selectedNames }
    return applied.sortedWith(order) + rest.sortedWith(order)
}

@Preview(showBackground = true)
@Composable
private fun CategoriesViewPreview() {
    ShioriTheme {
        CategoriesView(
            onDismiss = {},
            uniqueCategories = listOf(
                Tag(id = 1, name = "android", selected = false, nBookmarks = 214),
                Tag(id = 2, name = "design", selected = false, nBookmarks = 167),
                Tag(id = 3, name = "golang", selected = false, nBookmarks = 98),
                Tag(id = 4, name = "kotlin", selected = false, nBookmarks = 91),
                Tag(id = 5, name = "compose", selected = false, nBookmarks = 88),
            ),
            tagToHide = null,
            onFilterHiddenTag = {},
            selectedOptionIndex = 0,
            onSelectedOptionIndexChanged = { },
            selectedTags = listOf(Tag(id = 2, name = "design", selected = true, nBookmarks = 167)),
            onCategorySelected = { },
            onCategoryDeselected = { },
            onResetAll = { }
        )
    }
}
