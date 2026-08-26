package com.desarrollodroide.pagekeeper.ui.bookmarkeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import com.desarrollodroide.pagekeeper.ui.components.shouldSplitFormIntoTwoColumns
import androidx.compose.material3.Scaffold
import com.desarrollodroide.pagekeeper.ui.components.shouldSplitFormIntoTwoColumns
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
 * Laid out as labelled sections rather than one undifferentiated column of controls: a heading,
 * then the thing it describes, with the switches grouped inside a single rounded surface so they
 * read as one area rather than three loose rows.
 *
 * Fields are filled, not outlined. A form of outlined boxes stacked on a plain background is
 * mostly borders, and a border round every control says nothing about which one matters.
 *
 * Everything that is a button looks like one. The tag add is a tonal button rather than bare text,
 * and the bottom bar holds the one action this screen exists for, above the navigation bar rather
 * than jammed against the bottom edge of the screen.
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
    // feed it sits in a container the host scaffold has already padded, and applying them again
    // left a status bar's worth of empty space above the title.
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    val isManual = bookmarkEditorType == BookmarkEditorType.ADD_MANUALLY
    val isEdit = bookmarkEditorType == BookmarkEditorType.EDIT
    // Not just "not blank". Sending something unfetchable gets a 502 with an empty body, which
    // surfaced as a bookmark stuck pending in the feed and nothing on screen to explain it.
    val canSave = !isManual || isPlausibleBookmarkUrl(url)

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
            // save button living inside the scroll could be several screens down. The navigation
            // bar inset is applied here, otherwise it sits on the very bottom edge and reads as
            // half off the screen.
            Surface(color = MaterialTheme.colorScheme.surface) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // One button, full width. A Clear beside it took half the bar to offer
                    // throwing away what you had just typed, which is not worth equal weight with
                    // saving, and in edit mode it was permanently disabled: a greyed control that
                    // never does anything is worse than no control. Back already discards.
                    Button(
                        onClick = { saveBookmark(bookmarkEditorType) },
                        enabled = canSave,
                        modifier = Modifier
                            .widthIn(max = ContentMaxWidth)
                            .fillMaxWidth()
                            .height(ActionButtonHeight),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Text(
                            text = if (isEdit) "Save" else "Add bookmark",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // Declared once, laid out two ways below.
        val linkSection: @Composable () -> Unit = {
            Section(title = "Link") {
                if (isManual) {
                    FilledField(
                        value = url,
                        onValueChange = onUrlChange,
                        placeholder = "example.com",
                        leadingIcon = Icons.Outlined.Link,
                        isError = url.isNotBlank() && !canSave,
                        supportingText = if (url.isNotBlank() && !canSave) {
                            "That does not look like a link"
                        } else {
                            "https:// is added if you leave it out"
                        },
                        // Without these the keyboard capitalises and autocorrects: typing
                        // google.es actually sent Google.es.
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        ),
                    )
                } else {
                    // The shared url cannot be edited here, so it reads as the link it is
                    // rather than pretending to be a field.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = url,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
        val optionsSection: @Composable () -> Unit = {
            Section(title = "Options") {
                // One surface around all of them, so they read as a group rather than as loose
                // rows floating on the page.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        if (!isEdit) {
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
                    }
                }
            }
        }
        val tagsSection: @Composable () -> Unit = {
            Section(title = "Tags") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledField(
                        value = newTag.value,
                        onValueChange = { newTag.value = it },
                        placeholder = "New tag",
                        leadingIcon = Icons.AutoMirrored.Outlined.Label,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                        ),
                    )
                    // A tonal button: as bare text beside a field it did not read as something
                    // you press.
                    FilledTonalButton(
                        onClick = ::addTypedTag,
                        enabled = newTag.value.isNotBlank(),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.height(FieldHeight),
                    ) {
                        Text("Add")
                    }
                }

                if (assignedTags.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    RemovableTagsView(
                        tags = assignedTags.value,
                        onRemove = { removed ->
                            assignedTags.value = assignedTags.value.filter { it != removed }
                        },
                    )
                }

                val suggestions = availableTags.value.filter { it !in assignedTags.value }
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Suggestions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TagsSelectorView(
                        availableTags = suggestions,
                        onTagSelected = {
                            if (!assignedTags.value.contains(it)) {
                                assignedTags.value = assignedTags.value + it
                            }
                        }
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (shouldSplitFormIntoTwoColumns(maxWidth, maxHeight)) {
                // A tablet in landscape has about 540dp of height and 960dp of width. One tall
                // column put two thirds of the width to no use at all and still could not fit the
                // form: Tags was cut in half by the save bar and everything below it was off
                // screen. Two columns spend the width instead of the height.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top,
                ) {
                    // 16dp rather than the 24dp the single column uses. At 540dp of height the
                    // three add-time switches came to about 20dp more than the column had, and a
                    // card clipped by the save bar looks broken even though it scrolls.
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        linkSection()
                        optionsSection()
                    }
                    // Tags gets its own column because it is the half that grows: assigned chips
                    // and suggestions have no fixed height, and the other two do.
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        tagsSection()
                    }
                }
            } else {
                Column(
                    // Capped and centred: on a tablet or an unfolded foldable the fields otherwise
                    // ran the whole 2000px. Scrollable because the tag list grows and a landscape
                    // phone with the keyboard up has very little height left.
                    modifier = Modifier
                        .widthIn(max = ContentMaxWidth)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    linkSection()
                    optionsSection()
                    tagsSection()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/** A heading and the thing it describes. */
@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        content()
    }
}

/** A filled field with no indicator line. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilledField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
private fun RemovableTagsView(
    tags: List<Tag>,
    onRemove: (Tag) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        availableTags.forEach { category ->
            SuggestionChip(
                onClick = { onTagSelected(category) },
                label = { Text(category.name) },
                shape = MaterialTheme.shapes.large,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = null,
            )
        }
    }
}

/** Tall enough to sit level with a filled field, and to read as a target rather than a label. */
private val FieldHeight = 56.dp
private val ActionButtonHeight = 52.dp

@Preview(showBackground = true)
@Composable
fun BookmarkEditorPreview() {
    val tag1 = Tag(id = 1, name = "android", selected = true, nBookmarks = 0)
    val tag2 = Tag(id = 2, name = "compose", selected = false, nBookmarks = 0)
    val assignedTags = remember { mutableStateOf(listOf(tag1)) }
    val newTag = remember { mutableStateOf("") }

    BookmarkEditorView(
        title = "Add bookmark",
        url = "",
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
