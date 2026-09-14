package com.desarrollodroide.pagekeeper.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewCompactAlt
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    useTwoPaneLayout: Boolean,
    onUseTwoPaneLayoutChanged: (Boolean) -> Unit,
    onClickHideDialogOption: () -> Unit,
    onHideTagChanged: (Tag?) -> Unit,
    tagsUiState: UiState<List<Tag>>,
    hideTag: Tag?,
    onNavigateToTags: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Open only because the user asked. It used to open whenever tags were loaded, and the view
    // model keeps them once fetched, so a rotation or a trip to Manage tags brought back a sheet
    // the user had closed. Saveable, so a rotation keeps it the way the user left it.
    var hideTagSheetRequested by rememberSaveable { mutableStateOf(false) }

    SettingsGroup(title = "Bookmark list", modifier = modifier) {
        SwitchOption(
            title = "Compact view",
            icon = Icons.Filled.ViewCompactAlt,
            checked = compactView,
            onCheckedChange = onCompactViewChanged
        )
        SwitchOption(
            title = "Two panes on large screens",
            icon = Icons.Filled.ViewColumn,
            subtitle = "Read an article beside the list instead of over it",
            checked = useTwoPaneLayout,
            onCheckedChange = onUseTwoPaneLayoutChanged
        )
        ClickableOption(
            title = "Manage tags",
            icon = Icons.Filled.Style,
            subtitle = "Rename or delete tags",
            onClick = onNavigateToTags
        )
        ClickableOption(
            title = "Hide tag",
            icon = Icons.Filled.Sell,
            subtitle = hideTag?.name ?: "None",
            onClick = {
                hideTagSheetRequested = true
                onClickHideDialogOption()
            }
        )
    }

    if (tagsUiState.isLoading) {
        InfiniteProgressDialog(onDismissRequest = {})
    }

    val sheetStateCategories = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    if (hideTagSheetRequested && !tagsUiState.isLoading && tagsUiState.data != null) {
        val scope = rememberCoroutineScope()
        ModalBottomSheet(
            shape = BottomSheetDefaults.ExpandedShape,
            onDismissRequest = { hideTagSheetRequested = false },
            sheetState = sheetStateCategories,
        ) {
            val categories: List<Tag> = tagsUiState.data ?: emptyList()
            HideCategoryOptionView(
                hideTag = hideTag,
                uniqueCategories = categories,
                onApply = { selectedTag ->
                    scope.launch {
                        sheetStateCategories.hide()
                        hideTagSheetRequested = false
                        onHideTagChanged(selectedTag)
                    }
                },
            )
        }
    }
}
