package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * The library's tools, floating at the bottom with the add button attached.
 *
 * These used to be app bar actions. Down here they are within reach of a thumb, and the header is
 * left with search and the tag filter, which are about what the list shows rather than about the
 * app.
 *
 * Sync is always here now, where the app bar only showed it while something was queued: the sheet
 * is also the place to see that nothing is waiting, and a button that comes and goes moves the
 * others around under the finger.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun LibraryToolbar(
    onAddClick: () -> Unit,
    onTagsClick: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    pendingJobs: List<PendingJob>,
    modifier: Modifier = Modifier,
) {
    HorizontalFloatingToolbar(
        expanded = true,
        floatingActionButton = {
            // Coral, the warm accent: the one action that adds to the library stands apart from
            // the teal of everything that browses it.
            FloatingToolbarDefaults.VibrantFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add bookmark")
            }
        },
        modifier = modifier,
        // Inverse surface rather than the standard container: the toolbar floats over cards that
        // are themselves tonal surfaces, and it has to read as a separate layer on top of them.
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = MaterialTheme.colorScheme.inverseSurface,
            toolbarContentColor = MaterialTheme.colorScheme.inverseOnSurface,
            fabContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            fabContentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        IconButton(onClick = onTagsClick) {
            Icon(Icons.Outlined.Sell, contentDescription = "Tags")
        }
        SyncButton(pendingJobs = pendingJobs, onClick = onSyncClick)
        IconButton(onClick = onSettingsClick) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
        }
    }
}

/**
 * What can be done to the picked bookmarks, in the place the library's tools float: add tags,
 * update their cache, delete them. Labelled, unlike the library's tools, because these act on
 * several bookmarks at once and a wrong guess costs more.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SelectionToolbar(
    onAddTags: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        colors = FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = scheme.inverseSurface,
            toolbarContentColor = scheme.inverseOnSurface,
        ),
    ) {
        SelectionAction(icon = Icons.Outlined.Sell, label = "Add tags", onClick = onAddTags)
        SelectionAction(icon = Icons.Outlined.CloudUpload, label = "Update", onClick = onUpdate)
        SelectionAction(
            icon = Icons.Outlined.Delete,
            label = "Delete",
            onClick = onDelete,
            containerColor = scheme.error,
            contentColor = scheme.onError,
        )
    }
}

@Composable
private fun SelectionAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = Color.Transparent,
    contentColor: Color = LocalContentColor.current,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

/** Opens the sync sheet. Counts what is queued, and turns while a job is actually running. */
@Composable
private fun SyncButton(
    pendingJobs: List<PendingJob>,
    onClick: () -> Unit,
) {
    val hasRunningJobs = pendingJobs.any { it.state.uppercase() == "RUNNING" }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(hasRunningJobs) {
        if (hasRunningJobs) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            )
        } else {
            rotation.snapTo(0f)
        }
    }
    BadgedIconButton(
        onClick = onClick,
        badgeCount = pendingJobs.size,
        description = if (pendingJobs.isEmpty()) "Sync" else "Sync, ${pendingJobs.size} pending",
    ) {
        Icon(
            imageVector = Icons.Default.Sync,
            contentDescription = null,
            modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
        )
    }
}

@Preview
@Composable
private fun LibraryToolbarPreview() {
    ShioriTheme {
        LibraryToolbar(
            onAddClick = { },
            onTagsClick = { },
            onSyncClick = { },
            onSettingsClick = { },
            pendingJobs = emptyList(),
        )
    }
}
