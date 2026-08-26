package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag

/**
 * Tags rendered as M3 assist chips rather than hand-rolled rounded [Text] blocks — chips bring the
 * correct touch target, state layers and ripple for free.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClickableCategoriesView(
    uniqueCategories: List<Tag>,
    onClickCategory: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        // The card reserves exactly one chip row. Wrapping onto a second would push the card past
        // its neighbours in the grid row, which is the raggedness this is meant to avoid.
        maxLines = 1,
    ) {
        uniqueCategories.forEach { category ->
            AssistChip(
                onClick = { onClickCategory(category) },
                label = { Text(category.name) },
                shape = MaterialTheme.shapes.small,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                border = null,
            )
        }
    }
}
