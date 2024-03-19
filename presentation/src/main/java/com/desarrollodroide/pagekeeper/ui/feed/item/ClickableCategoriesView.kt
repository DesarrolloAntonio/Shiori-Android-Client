package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ClickableCategoriesView(
    uniqueCategories: List<Tag>,
    onClickCategory: (Tag) -> Unit
) {
    FlowRow(
    ) {
        uniqueCategories.forEach { category ->
            Text(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(5.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .clickable { onClickCategory(category) }
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                text = category.name
            )
        }
    }
}
