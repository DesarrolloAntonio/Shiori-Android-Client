package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
    modifier: Modifier = Modifier,
) {
    val isCategoriesVisible = remember { mutableStateOf(false) }

    SettingsGroup(title = "Bookmark list", modifier = modifier) {
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
    }

    if (tagsUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }

    val sheetStateCategories = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(tagsUiState) {
        if (tagsUiState.data != null) {
            isCategoriesVisible.value = true
        }
    }

    if (isCategoriesVisible.value) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            shape = BottomSheetDefaults.ExpandedShape,
            onDismissRequest = { isCategoriesVisible.value = false },
            sheetState = sheetStateCategories,
        ) {
            val categories: List<Tag> = tagsUiState.data ?: emptyList()
            HideCategoryOptionView(
                hideTag = hideTag,
                uniqueCategories = categories,
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
