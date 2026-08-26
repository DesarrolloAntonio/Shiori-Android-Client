package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * Search app bar.
 *
 * M3 names this variant for exactly this case: "use on home pages when search is key to the
 * product", with a search field in place of heading text, a product logo as the leading element
 * and at most two trailing icons. It replaces both the two row flexible bar and the separate
 * search icon, so the feed gets one 64dp row of chrome instead of roughly 230dp of bar top and
 * bottom.
 *
 * The guidance also says an app bar should carry one or two actions and that anything more
 * belongs elsewhere, which is what forced the choice here: filter and settings are the two, add
 * is the fab, and sync only appears when there is actually something pending.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSyncClick: () -> Unit,
    selectedTagsCount: Int,
    showOnlyHiddenTag: Boolean,
    pendingJobsCount: Int,
    pendingJobs: List<PendingJob>,
) {
    val hasRunningJobs = pendingJobs.any { it.state.uppercase() == "RUNNING" }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(hasRunningJobs) {
        if (hasRunningJobs) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            rotation.snapTo(0f)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_pagekeeper),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .size(32.dp)
            )
            // The field is the headline, and it is a real field. It used to be a Surface styled to
            // look like one whose only job was to open a full screen search sheet: it invited
            // typing and answered with a different screen carrying a second, identical looking box.
            // Typing here filters the feed underneath instead, which is what the web does.
            // surfaceContainerHigh keeps it distinct from the app background, per the spec.
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Search bookmarks" },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        // The results are already on screen, so the only thing left to do on
                        // Search is get the keyboard out of the way.
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search bookmarks",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            innerTextField()
                        },
                    )
                    // Takes no room until there is something to clear, so an empty field keeps the
                    // full width for the placeholder.
                    AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onClearSearch()
                                keyboardController?.hide()
                            },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
            // Sync earns its place only while something is queued, so the bar stays at two
            // actions the rest of the time.
            AnimatedVisibility(visible = pendingJobsCount > 0) {
                BadgedIconButton(
                    onClick = onSyncClick,
                    badgeCount = pendingJobsCount,
                    description = "Pending sync",
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer { rotationZ = rotation.value },
                    )
                }
            }
            BadgedIconButton(
                onClick = onFilterClick,
                badgeCount = if (showOnlyHiddenTag) 0 else selectedTagsCount,
                description = if (showOnlyHiddenTag) "Hidden tags" else "Filter by tag",
            ) {
                Icon(
                    imageVector = if (showOnlyHiddenTag) Icons.Default.VisibilityOff else Icons.Outlined.Sell,
                    contentDescription = null,
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    }
}

/** Icon button that carries a count badge, animated in and out. */
/**
 * App bar while bookmarks are selected.
 *
 * Replaces the search bar rather than sitting beside it, so it is obvious that the buttons act on
 * the selection and not on the feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onDelete: () -> Unit,
    onUpdateCache: () -> Unit,
    onAddTags: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = "$selectedCount selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cancel selection")
            }
        },
        actions = {
            IconButton(onClick = onAddTags) {
                Icon(Icons.Outlined.Sell, contentDescription = "Add tags to selected")
            }
            IconButton(onClick = onUpdateCache) {
                Icon(Icons.Outlined.CloudUpload, contentDescription = "Update selected")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete selected",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    )
}

@Composable
private fun BadgedIconButton(
    onClick: () -> Unit,
    badgeCount: Int,
    description: String,
    icon: @Composable () -> Unit,
) {
    BadgedBox(
        badge = {
            androidx.compose.animation.AnimatedVisibility(
                visible = badgeCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Badge {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    ) {
        // The description goes on the button, not the glyph, so TalkBack announces the action once
        // ("Sync") rather than describing the icon inside a separately-labelled clickable.
        IconButton(
            onClick = onClick,
            modifier = Modifier.semantics { this.contentDescription = description },
        ) {
            icon()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    ShioriTheme {
        TopBar(
            searchQuery = "",
            onSearchQueryChange = { },
            onClearSearch = { },
            onFilterClick = { },
            onSettingsClick = { },
            onSyncClick = { },
            selectedTagsCount = 0,
            showOnlyHiddenTag = false,
            pendingJobsCount = 0,
            pendingJobs = emptyList(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarWithPendingPreview() {
    ShioriTheme {
        TopBar(
            searchQuery = "kotlin",
            onSearchQueryChange = { },
            onClearSearch = { },
            onFilterClick = { },
            onSettingsClick = { },
            onSyncClick = { },
            selectedTagsCount = 2,
            showOnlyHiddenTag = false,
            pendingJobsCount = 3,
            pendingJobs = emptyList(),
        )
    }
}

/** The other state of the same slot, so the two can be compared side by side in the preview pane. */
@Preview(name = "Selection bar", showBackground = true)
@Composable
private fun SelectionTopBarPreview() {
    ShioriTheme {
        SelectionTopBar(
            selectedCount = 3,
            onClose = { },
            onDelete = { },
            onUpdateCache = { },
            onAddTags = { },
        )
    }
}
