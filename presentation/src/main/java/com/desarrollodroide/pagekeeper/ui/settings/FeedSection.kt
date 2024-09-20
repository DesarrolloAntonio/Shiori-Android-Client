package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.InfiniteProgressDialog
import com.desarrollodroide.pagekeeper.ui.components.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedSection(
    compactView: Boolean,
    onCompactViewChanged: (Boolean) -> Unit,
    onClickHideDialogOption: () -> Unit,
    onHideTagChanged: (Tag?) -> Unit,
    tagsUiState: UiState<List<Tag>>,
    hideTag: Tag?,
    ) {
    val isCategoriesVisible = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 5.dp)
    ) {
        Text(
            text = "Bookmark list",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(5.dp))
        SwitchOption(
            title = "Compact view",
            icon = Icons.Filled.ViewCompactAlt,
            checked = compactView,
            onCheckedChange = onCompactViewChanged
        )
        ClickableOption(
            title = "Hide tag",
            icon = Icons.Filled.Sell,
            subtitle = hideTag?.name ?: "None",
            onClick = onClickHideDialogOption
        )

        if (tagsUiState.isLoading) {
            InfiniteProgressDialog(onDismissRequest = {})
        }

        val sheetStateCategories = rememberModalBottomSheetState(
            skipPartiallyExpanded = false
        )

        LaunchedEffect(tagsUiState) {
            if (tagsUiState.data != null) {
                isCategoriesVisible.value = true
            }
        }

        if (isCategoriesVisible.value) {
            val scope = rememberCoroutineScope()
            ModalBottomSheet(
                shape = BottomSheetDefaults.ExpandedShape,
                onDismissRequest = {
                    isCategoriesVisible.value = false
                },
                sheetState = sheetStateCategories,
            ) {
                val categories: List<Tag> = tagsUiState.data ?: emptyList()
                val categoriesState = remember { mutableStateOf(categories) }
                HideCategoryOptionView(
                    hideTag = hideTag,
                    uniqueCategories = categoriesState,
                    onApply = { selectedTag ->
                        scope.launch {
                            sheetStateCategories.hide()
                            isCategoriesVisible.value = false
                            onHideTagChanged(selectedTag)
                        }
                    },
                )
            }
        }
    }
}