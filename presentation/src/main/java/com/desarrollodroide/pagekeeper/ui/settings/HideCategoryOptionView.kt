package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.TagSaver
import com.desarrollodroide.pagekeeper.ui.feed.TagFilterChip
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * Settings › Hide tag: one tag whose bookmarks are kept out of the library.
 *
 * Drawn like the filter sheet it sits beside in the app — a heavy title, the same tag chips (the
 * chosen one filled, with a check), and a bottom bar where the main action is the filled one. It
 * used to be a centred title over outlined chips, with "None" and "Apply" as two identical filled
 * buttons, so nothing said which one saved the choice.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HideCategoryOptionView(
    onApply: (Tag?) -> Unit,
    uniqueCategories: List<Tag>,
    hideTag: Tag?
) {
    var selectedTag by rememberSaveable(stateSaver = TagSaver) { mutableStateOf(hideTag) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Text(
                text = "Select category to hide",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "Bookmarks with this tag are kept out of the library.",
            modifier = Modifier.padding(top = 10.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (uniqueCategories.isEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sell,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
                Text("No categories available", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Scrolls inside the sheet, so a long list of tags never pushes the buttons off it.
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uniqueCategories.forEach { tag ->
                    val selected = selectedTag?.id == tag.id
                    TagFilterChip(
                        tag = tag,
                        selected = selected,
                        onClick = { selectedTag = if (selected) null else tag },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Clears the hidden tag outright, so it is the quieter of the two.
            TextButton(
                onClick = {
                    selectedTag = null
                    onApply(null)
                },
                modifier = Modifier.height(56.dp),
                shape = CircleShape,
            ) {
                Text("None", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { onApply(selectedTag) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = CircleShape,
                enabled = uniqueCategories.isNotEmpty(),
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HideCategoryOptionViewPreview() {
    val regionOptions =
            listOf(
                Tag(id = 1, name = "Northern Europe"), Tag(id = 2, name = "Western Europe"),
                Tag(id = 3, name = "Southern Europe"), Tag(id = 4, name = "Southeast Europe"),
                Tag(id = 5, name = "Central Europe"), Tag(id = 6, name = "Eastern Europe")
            )

    ShioriTheme {
        HideCategoryOptionView(
            onApply = {},
            uniqueCategories = regionOptions,
            hideTag = regionOptions[1]
        )
    }
}
