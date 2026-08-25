package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.ContentMaxWidth
import com.desarrollodroide.pagekeeper.ui.settings.SwitchOption

enum class BookmarkEditorType { ADD, ADD_MANUALLY, EDIT }

/**
 * The add and edit bookmark form.
 *
 * A Scaffold with a real TopAppBar rather than the Row that used to stand in for one. That Row had
 * no window insets, and since targetSdk 35 makes edge to edge mandatory the whole header drew
 * underneath the status bar: back and save sat entirely inside the top 136px on a Pixel Fold, so
 * neither could be tapped at all. A TopAppBar applies the inset itself.
 *
 * Save is a button in a bottom bar now, not a floppy disk icon in the corner. It is the only thing
 * this screen exists to do, it stays put while the form scrolls, and it can say whether it is
 * available.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkEditorView(
    title: String,
    url: String,
    bookmarkEditorType: BookmarkEditorType,
    newTag: MutableState<String>,
    assignedTags: MutableState<List<Tag>>,
    availableTags: State<List<Tag>>,
    saveBookmark: (BookmarkEditorType) -> Unit,
    onBackClick: () -> Unit,
    createArchive: Boolean,
    onCreateArchiveChanged: (Boolean) -> Unit,
    makeArchivePublic: Boolean,
    onMakeArchivePublicChanged: (Boolean) -> Unit,
    createEbook: Boolean,
    onCreateEbookChanged: (Boolean) -> Unit,
    onUrlChange: (String) -> Unit = {},
    // The activity draws edge to edge and needs the bars accounted for. Opened from inside the
    // feed it sits in a container that has already been padded by the host scaffold, and applying
    // them again left a status bar's worth of empty space above the title.
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val isManual = bookmarkEditorType == BookmarkEditorType.ADD_MANUALLY
    val canSave = !isManual || url.isNotBlank()

    fun addTypedTag() {
        val normalizedName = newTag.value.lowercase().trim()
        if (normalizedName.isNotBlank() &&
            assignedTags.value.none { it.name.lowercase() == normalizedName }
        ) {
            assignedTags.value = assignedTags.value + Tag(id = -1, name = normalizedName)
            newTag.value = ""
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = windowInsets,
        topBar = {
            TopAppBar(
                windowInsets = windowInsets.only(WindowInsetsSides.Top),
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            // Pinned rather than at the end of the form: the tag list grows without limit, so a
            // save button living inside the scroll could be several screens down.
            Surface(color = MaterialTheme.colorScheme.surface) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = { saveBookmark(bookmarkEditorType) },
                        enabled = canSave,
                        modifier = Modifier
                            .widthIn(max = ContentMaxWidth)
                            .fillMaxWidth(),
                    ) {
                        Text(if (bookmarkEditorType == BookmarkEditorType.EDIT) "Save" else "Add bookmark")
                    }
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                // Capped and centred: on a tablet or an unfolded foldable the fields otherwise ran
                // the whole 2000px. Scrollable because the tag list grows and a landscape phone
                // with the keyboard up has very little height left.
                modifier = Modifier
                    .widthIn(max = ContentMaxWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                if (isManual) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = onUrlChange,
                        label = { Text("URL") },
                        placeholder = { Text("https://") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                } else {
                    // The shared url is not editable here, so it reads as a label rather than
                    // pretending to be a field.
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Link",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // The same switch rows settings uses, rather than bare checkboxes with a label
                // floating beside them.
                if (bookmarkEditorType != BookmarkEditorType.EDIT) {
                    SwitchOption(
                        title = "Create archive",
                        subtitle = "Keep an offline copy of the page",
                        icon = Icons.Outlined.Archive,
                        checked = createArchive,
                        onCheckedChange = onCreateArchiveChanged,
                    )
                    SwitchOption(
                        title = "Create ebook",
                        subtitle = "Also save it as an epub",
                        icon = Icons.Outlined.MenuBook,
                        checked = createEbook,
                        onCheckedChange = onCreateEbookChanged,
                    )
                }
                SwitchOption(
                    title = "Public",
                    subtitle = "Anyone with the link can read it",
                    icon = Icons.Outlined.Public,
                    checked = makeArchivePublic,
                    onCheckedChange = onMakeArchivePublicChanged,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = newTag.value,
                        onValueChange = { newTag.value = it },
                        label = { Text("New tag") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null)
                        },
                    )
                    TextButton(
                        onClick = ::addTypedTag,
                        enabled = newTag.value.isNotBlank(),
                    ) {
                        Text("Add")
                    }
                }

                // Assigned tags sit as removable chips rather than inside a bordered scrolling
                // box, which read as a second, empty text field when the bookmark had no tags.
                if (assignedTags.value.isNotEmpty()) {
                    RemovableTagsView(
                        tags = assignedTags.value,
                        onRemove = { removed ->
                            assignedTags.value = assignedTags.value.filter { it != removed }
                        },
                    )
                }

                if (availableTags.value.isNotEmpty()) {
                    Text(
                        text = "All tags",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TagsSelectorView(
                        availableTags = availableTags.value,
                        onTagSelected = {
                            if (!assignedTags.value.contains(it)) {
                                assignedTags.value = assignedTags.value + it
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
private fun RemovableTagsView(
    tags: List<Tag>,
    onRemove: (Tag) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tags.forEach { tag ->
            InputChip(
                selected = true,
                onClick = { onRemove(tag) },
                label = { Text(tag.name) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove ${tag.name}",
                        modifier = Modifier.height(18.dp),
                    )
                },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TagsSelectorView(
    availableTags: List<Tag>,
    onTagSelected: (Tag) -> Unit
) {
    // These were a hardcoded light grey pill with dark grey text, which in dark theme rendered as
    // white blocks against the background. Chips take their colours from the scheme.
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        availableTags.forEach { category ->
            AssistChip(
                onClick = { onTagSelected(category) },
                label = { Text(category.name) },
                shape = MaterialTheme.shapes.small,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = null,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BookmarkEditorPreview() {
    val tag1 = Tag(id = 1, name = "android", selected = true, nBookmarks = 0)
    val tag2 = Tag(id = 2, name = "compose", selected = false, nBookmarks = 0)
    val assignedTags = remember { mutableStateOf(listOf(tag1, tag2)) }
    val newTag = remember { mutableStateOf("") }

    BookmarkEditorView(
        title = "Add manually",
        url = "http://www.google.com",
        bookmarkEditorType = BookmarkEditorType.ADD_MANUALLY,
        assignedTags = assignedTags,
        saveBookmark = {},
        availableTags = remember { mutableStateOf(listOf(tag1, tag2)) },
        newTag = newTag,
        onBackClick = {},
        makeArchivePublic = true,
        createArchive = false,
        createEbook = false,
        onMakeArchivePublicChanged = {},
        onCreateEbookChanged = {},
        onCreateArchiveChanged = {}
    )
}
