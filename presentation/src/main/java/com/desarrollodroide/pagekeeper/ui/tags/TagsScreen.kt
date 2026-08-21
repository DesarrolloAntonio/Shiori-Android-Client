package com.desarrollodroide.pagekeeper.ui.tags

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.tooling.preview.Preview
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.ContentMaxWidth
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagsScreen(
    tagsViewModel: TagsViewModel,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }

    val tags by tagsViewModel.tags.collectAsStateWithLifecycle()
    val uiState by tagsViewModel.tagsUiState.collectAsStateWithLifecycle()
    val actionError by tagsViewModel.actionError.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    var tagBeingEdited by remember { mutableStateOf<Tag?>(null) }
    var tagBeingDeleted by remember { mutableStateOf<Tag?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            tagsViewModel.clearActionError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Tags") },
                subtitle = {
                    if (tags.isNotEmpty()) {
                        Text(
                            text = if (tags.size == 1) "1 tag" else "${tags.size} tags",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isCreating = true },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = "New tag",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter,
        ) {
            when {
                tags.isEmpty() && uiState.isLoading -> {
                    ContainedLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }

                tags.isEmpty() -> {
                    EmptyTags(modifier = Modifier.align(Alignment.Center))
                }

                else -> {
                    TagsList(
                        tags = tags,
                        onRename = { tagBeingEdited = it },
                        onDelete = { tagBeingDeleted = it },
                    )
                }
            }
        }
    }

    if (isCreating) {
        TagNameDialog(
            title = "New tag",
            confirmLabel = "Create",
            initialName = "",
            existingNames = tags.map { it.name },
            onDismiss = { isCreating = false },
            onConfirm = { name ->
                isCreating = false
                tagsViewModel.createTag(name)
            },
        )
    }

    tagBeingEdited?.let { tag ->
        TagNameDialog(
            title = "Rename tag",
            confirmLabel = "Rename",
            initialName = tag.name,
            existingNames = tags.map { it.name } - tag.name,
            onDismiss = { tagBeingEdited = null },
            onConfirm = { name ->
                tagBeingEdited = null
                tagsViewModel.renameTag(tag, name)
            },
        )
    }

    tagBeingDeleted?.let { tag ->
        DeleteTagDialog(
            tag = tag,
            onDismiss = { tagBeingDeleted = null },
            onConfirm = {
                tagBeingDeleted = null
                tagsViewModel.deleteTag(tag)
            },
        )
    }
}

@Composable
private fun TagsList(
    tags: List<Tag>,
    onRename: (Tag) -> Unit,
    onDelete: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .widthIn(max = ContentMaxWidth)
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            // room for the extended fab
            bottom = 96.dp,
        ),
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column {
                    tags.forEach { tag ->
                        TagRow(
                            tag = tag,
                            onRename = { onRename(tag) },
                            onDelete = { onDelete(tag) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TagRow(
    tag: Tag,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ListItem(
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Icon(
                Icons.Outlined.Sell,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        headlineContent = { Text(tag.name, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = {
            Text(
                text = when (tag.nBookmarks) {
                    0 -> "No bookmarks"
                    1 -> "1 bookmark"
                    else -> "${tag.nBookmarks} bookmarks"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Actions for ${tag.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            menuExpanded = false
                            onRename()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                    )
                }
            }
        },
    )
}

@Composable
private fun EmptyTags(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Icon(
                modifier = Modifier.padding(28.dp),
                imageVector = Icons.Outlined.Sell,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "No tags yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Tags you add to bookmarks show up here, and you can rename or remove them.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TagsListPreview() {
    ShioriTheme {
        TagsList(
            tags = listOf(
                Tag(id = 1, name = "android", selected = false, nBookmarks = 42),
                Tag(id = 2, name = "kotlin", selected = false, nBookmarks = 1),
                Tag(id = 3, name = "compose", selected = false, nBookmarks = 0),
                Tag(id = 4, name = "self-hosted", selected = false, nBookmarks = 7),
            ),
            onRename = {},
            onDelete = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 520)
@Composable
private fun EmptyTagsPreview() {
    ShioriTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            EmptyTags(modifier = Modifier.align(Alignment.Center))
        }
    }
}
