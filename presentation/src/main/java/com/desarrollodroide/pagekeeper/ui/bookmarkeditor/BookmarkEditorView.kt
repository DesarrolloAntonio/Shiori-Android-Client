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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.sp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.ContentMaxWidth
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkThumbnailPlaceholder
import com.desarrollodroide.pagekeeper.ui.feed.item.hostOf
import com.desarrollodroide.pagekeeper.ui.settings.SettingsAccents
import com.desarrollodroide.pagekeeper.ui.settings.SettingsRows
import com.desarrollodroide.pagekeeper.ui.settings.SwitchOption
import com.desarrollodroide.pagekeeper.ui.theme.keyedColors

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
            EditorHeader(
                title = title,
                onBackClick = onBackClick,
                windowInsets = windowInsets,
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
                        shape = CircleShape,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        Text(
                            text = if (isEdit) "Save" else "Add bookmark",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        // Declared once, laid out two ways below.
        val linkSection: @Composable () -> Unit = {
            Section(title = "Link", color = MaterialTheme.colorScheme.primary) {
                if (isManual) {
                    FilledField(
                        value = url,
                        onValueChange = onUrlChange,
                        placeholder = "example.com",
                        leadingIcon = Icons.Outlined.Link,
                        isError = url.isNotBlank() && !canSave,
                        // A check once the link looks fetchable, so the field confirms as well as
                        // complains.
                        trailingIcon = if (url.isNotBlank() && canSave) Icons.Filled.CheckCircle else null,
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
                    // The shared url cannot be edited here, so it reads as the link it is rather
                    // than pretending to be a field: the site's monogram and name, then the url.
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BookmarkThumbnailPlaceholder(url = url, size = 56.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hostOf(url).ifEmpty { "Link" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = url,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
        val optionsSection: @Composable () -> Unit = {
            Section(title = "Options", color = MaterialTheme.colorScheme.secondary) {
                // Connected rows, the way Settings draws its groups, in this section's indigo.
                SettingsRows(accent = SettingsAccents.visual) {
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
        val tagsSection: @Composable () -> Unit = {
            Section(title = "Tags", color = MaterialTheme.colorScheme.tertiary) {
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

/**
 * The screen's header: back in a tonal circle, the title set large, and a coral shape bleeding off
 * the top corner, the same way the library and the other Expressive screens open.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EditorHeader(
    title: String,
    onBackClick: () -> Unit,
    windowInsets: WindowInsets,
) {
    val decoration = MaterialShapes.Flower.toShape()
    val decorationColor = MaterialTheme.colorScheme.tertiaryContainer
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .drawBehind {
                    val side = 260.dp.toPx()
                    val outline = decoration.createOutline(Size(side, side), layoutDirection, this)
                    translate(left = size.width - side + 100.dp.toPx(), top = -side / 2f) {
                        drawOutline(outline, decorationColor)
                    }
                }
                .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Top)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
            )
        }
    }
}

/** A heading, in its section's colour, and the thing it describes. */
@Composable
private fun Section(
    title: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
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
    trailingIcon: ImageVector? = null,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        trailingIcon = trailingIcon?.let {
            { Icon(it, contentDescription = "Valid link", tint = MaterialTheme.colorScheme.primary) }
        },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = true,
        shape = MaterialTheme.shapes.large,
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
            // Each tag in the colour it has everywhere else.
            val colors = keyedColors(tag.name)
            InputChip(
                selected = true,
                onClick = { onRemove(tag) },
                label = { Text(tag.name, fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove ${tag.name}",
                        modifier = Modifier.height(18.dp),
                    )
                },
                shape = MaterialTheme.shapes.small,
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = colors.container,
                    selectedLabelColor = colors.content,
                    selectedTrailingIconColor = colors.content,
                ),
                border = null,
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
private val ActionButtonHeight = 60.dp

@Preview(showBackground = true)
@Composable
private fun BookmarkEditorPreview() {
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
