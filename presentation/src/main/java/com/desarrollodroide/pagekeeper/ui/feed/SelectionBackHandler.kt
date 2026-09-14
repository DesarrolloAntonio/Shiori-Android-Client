package com.desarrollodroide.pagekeeper.ui.feed

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

/**
 * Back while bookmarks are selected clears the selection.
 *
 * The feed had no handler of its own, so Back reached the graph's, which finishes the activity:
 * one press in selection mode closed the app.
 */
@Composable
internal fun SelectionBackHandler(selectionActive: Boolean, onClearSelection: () -> Unit) {
    BackHandler(enabled = selectionActive, onBack = onClearSelection)
}
