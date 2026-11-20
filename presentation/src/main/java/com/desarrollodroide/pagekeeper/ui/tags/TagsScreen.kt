package com.desarrollodroide.pagekeeper.ui.tags

import androidx.compose.material.icons.outlined.NewLabel
import com.desarrollodroide.pagekeeper.ui.components.DialogTone
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.components.ContentMaxWidth
import com.desarrollodroide.pagekeeper.ui.components.TagSaver
import com.desarrollodroide.pagekeeper.ui.feed.SortToggle
import com.desarrollodroide.pagekeeper.ui.feed.TagSearchField
import com.desarrollodroide.pagekeeper.ui.feed.filterAndSortTags
import com.desarrollodroide.pagekeeper.ui.feed.item.KeyedMonogram
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.pagekeeper.ui.theme.keyedColors
import kotlinx.coroutines.launch
import java.text.NumberFormat

/**
 * Every tag, built for a server with hundreds of them: a search at the top, most used or A–Z, and
 * in A–Z a letter index to jump with.
 *
 * [onOpenTag] is what tapping a row does: the library, narrowed to that tag. Without it the rows
 * are not clickable and the header does not promise they are.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagsScreen(
    tagsViewModel: TagsViewModel,
    onBack: () -> Unit,
    onOpenTag: ((Tag) -> Unit)? = null,
) {
    BackHandler { onBack() }

    val tags by tagsViewModel.tags.collectAsStateWithLifecycle()
    val uiState by tagsViewModel.tagsUiState.collectAsStateWithLifecycle()
    val actionError by tagsViewModel.actionError.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    var query by rememberSaveable { mutableStateOf("") }
    var sortByName by rememberSaveable { mutableStateOf(false) }

    // An open rename or delete dialog should still be open after a rotation or a fold.
    var tagBeingEdited by rememberSaveable(stateSaver = TagSaver) { mutableStateOf<Tag?>(null) }
    var tagBeingDeleted by rememberSaveable(stateSaver = TagSaver) { mutableStateOf<Tag?>(null) }
    var isCreating by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(actionError) {
        actionError?.let {
            snackbarHostState.showSnackbar(it)
            tagsViewModel.clearActionError()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TagsHeader(
                tagCount = tags.size,
                query = query,
                onQueryChange = { query = it },
                showSearch = tags.isNotEmpty(),
                canOpenTags = onOpenTag != null,
                onBack = onBack,
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isCreating = true },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    text = "New tag",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                // While searching, the list ends above the keyboard, so the last matches can still
                // be scrolled to.
                .consumeWindowInsets(paddingValues)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            TagsContent(
                tags = tags,
                isLoading = uiState.isLoading,
                loadError = uiState.error,
                onRetry = tagsViewModel::refresh,
                onRename = { tagBeingEdited = it },
                onDelete = { tagBeingDeleted = it },
                query = query,
                sortByName = sortByName,
                onSortChange = { sortByName = it },
                onOpen = onOpenTag,
            )
        }
    }

    if (isCreating) {
        TagNameDialog(
            title = "New tag",
            confirmLabel = "Create",
            icon = Icons.Outlined.NewLabel,
            tone = DialogTone.Brand,
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

/**
 * The indigo block at the top: back, "YOUR TAGS" with the count set large, and the search.
 *
 * The count collapses as the list moves up, the way the library's does, and a small "Tags" takes
 * its place beside the back button. The search stays: with two hundred tags it is the way around.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TagsHeader(
    tagCount: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    showSearch: Boolean,
    canOpenTags: Boolean,
    onBack: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val collapsedFraction = scrollBehavior.state.collapsedFraction
    val decoration = MaterialShapes.Flower.toShape()
    val decorationColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
    val badgeShape = MaterialShapes.Clover4Leaf.toShape()

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .drawBehind {
                    val side = DecorationSize.toPx()
                    val outline = decoration.createOutline(Size(side, side), layoutDirection, this)
                    translate(
                        left = size.width - side + DecorationOverhang.toPx(),
                        top = -DecorationOverhang.toPx(),
                    ) {
                        drawOutline(outline, decorationColor)
                    }
                },
        ) {
            // Behind the column, so as it fades on collapse the search field covers it.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 26.dp, end = 26.dp)
                    .size(76.dp)
                    .graphicsLayer { alpha = 1f - collapsedFraction }
                    .clip(badgeShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sell,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(bottom = 20.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledIconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Tags",
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .graphicsLayer { alpha = collapsedFraction },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                    )
                }
                CollapsingTagsHero(
                    tagCount = tagCount,
                    canOpenTags = canOpenTags,
                    scrollBehavior = scrollBehavior,
                )
                if (showSearch) {
                    Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
                        TagSearchField(
                            query = query,
                            onQueryChange = onQueryChange,
                            tagCount = tagCount,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingTagsHero(
    tagCount: Int,
    canOpenTags: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { HeroHeight.toPx() }
    val state = scrollBehavior.state
    SideEffect {
        if (state.heightOffsetLimit != -expandedHeightPx) {
            state.heightOffsetLimit = -expandedHeightPx
        }
    }
    val visibleHeight: Dp = with(density) { (expandedHeightPx + state.heightOffset).coerceAtLeast(0f).toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(visibleHeight)
            .clipToBounds(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .wrapContentHeight(align = Alignment.Bottom, unbounded = true)
                .padding(horizontal = 20.dp)
                .graphicsLayer { alpha = 1f - state.collapsedFraction },
        ) {
            Text(
                text = "YOUR TAGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.secondary,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // No "0 tags" in giant type while the first load runs; the body says what is going on.
                Text(
                    text = if (tagCount == 0) "Tags" else NumberFormat.getIntegerInstance().format(tagCount),
                    style = heroNumberStyle(),
                    maxLines = 1,
                )
                if (tagCount > 0) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = if (tagCount == 1) "tag" else "tags",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp,
                        )
                        if (canOpenTags) {
                            Text(
                                text = "tap one to filter the library",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun heroNumberStyle() = MaterialTheme.typography.displayLarge.copy(
    fontSize = 76.sp,
    lineHeight = 76.sp,
    fontWeight = FontWeight.Black,
    letterSpacing = (-3).sp,
)

/** What the tags screen shows: a spinner, the list, "No tags yet", or why the list could not load. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BoxScope.TagsContent(
    tags: List<Tag>,
    isLoading: Boolean,
    loadError: String?,
    onRetry: () -> Unit,
    onRename: (Tag) -> Unit,
    onDelete: (Tag) -> Unit,
    query: String = "",
    sortByName: Boolean = false,
    onSortChange: ((Boolean) -> Unit)? = null,
    onOpen: ((Tag) -> Unit)? = null,
) {
    // Decided on every tag, not the ones a search leaves: a search with no hits is "No tags
    // match", inside the list, never "No tags yet".
    when {
        tags.isEmpty() && isLoading -> {
            ContainedLoadingIndicator(modifier = Modifier.align(Alignment.Center))
        }

        // Before "No tags yet": with nothing cached, a failed load used to read as an empty list.
        tags.isEmpty() && loadError != null -> {
            TagsLoadError(modifier = Modifier.align(Alignment.Center), onRetry = onRetry)
        }

        tags.isEmpty() -> {
            EmptyTags(modifier = Modifier.align(Alignment.Center))
        }

        else -> {
            TagsList(
                tags = tags,
                onRename = onRename,
                onDelete = onDelete,
                query = query,
                sortByName = sortByName,
                onSortChange = onSortChange,
                onOpen = onOpen,
            )
        }
    }
}

@Composable
// internal rather than private so ActionMenuTest can drive the per-tag overflow menu.
internal fun TagsList(
    tags: List<Tag>,
    onRename: (Tag) -> Unit,
    onDelete: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    query: String = "",
    sortByName: Boolean = false,
    onSortChange: ((Boolean) -> Unit)? = null,
    onOpen: ((Tag) -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val entries = remember(tags, query, sortByName) {
        tagListEntries(filterAndSortTags(tags, emptySet(), query, sortByName), sortByName)
    }
    // Against every tag rather than the ones shown, so a bar keeps its length while searching.
    val mostUsed = remember(tags) { tags.maxOfOrNull { it.nBookmarks } ?: 0 }
    val leadingItems = if (onSortChange != null) 1 else 0
    val letters = remember(entries) { entries.filterIsInstance<TagListEntry.Letter>().map { it.letter } }
    val showIndex = sortByName && letters.size > 1

    // A new search or order starts at the top. Remembered across rotation, so turning the phone
    // does not count as a new search and lose the place.
    var shownFor by rememberSaveable { mutableStateOf("$sortByName|$query") }
    LaunchedEffect(query, sortByName) {
        val key = "$sortByName|$query"
        if (key != shownFor) {
            shownFor = key
            listState.scrollToItem(0)
        }
    }

    Box(
        modifier = modifier
            .widthIn(max = ContentMaxWidth)
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = if (showIndex) 44.dp else 16.dp,
                top = 8.dp,
                // room for the extended fab
                bottom = 104.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (onSortChange != null) {
                item(key = "sort", contentType = "sort") {
                    Box(modifier = Modifier.padding(top = 6.dp, bottom = 9.dp)) {
                        SortToggle(sortByName = sortByName, onChange = onSortChange)
                    }
                }
            }
            if (entries.isEmpty()) {
                item(key = "no-match", contentType = "no-match") {
                    Text(
                        text = "No tags match",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(entries, key = { it.key }, contentType = { it::class }) { entry ->
                when (entry) {
                    is TagListEntry.Letter -> LetterHeading(entry.letter)
                    is TagListEntry.Row -> TagRow(
                        tag = entry.tag,
                        first = entry.first,
                        last = entry.last,
                        // Most used is about how much each tag is used, so it draws it; A–Z is
                        // about finding one, and the bars would only be noise there.
                        usage = if (sortByName) null else usageOf(entry.tag.nBookmarks, mostUsed),
                        onOpen = onOpen?.let { open -> { open(entry.tag) } },
                        onRename = { onRename(entry.tag) },
                        onDelete = { onDelete(entry.tag) },
                    )
                }
            }
        }

        if (showIndex) {
            val current by remember(entries, leadingItems) {
                derivedStateOf { currentLetter(entries, listState, leadingItems) }
            }
            LetterIndex(
                letters = letters,
                current = current,
                onJump = { letter ->
                    val index = entries.indexOfFirst { it is TagListEntry.Letter && it.letter == letter }
                    if (index >= 0) scope.launch { listState.scrollToItem(index + leadingItems) }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

/** One line of the list: a letter heading in A–Z, or a tag, with where it sits in its group. */
internal sealed interface TagListEntry {
    val key: String

    data class Letter(val letter: Char) : TagListEntry {
        override val key: String get() = "letter-$letter"
    }

    data class Row(val tag: Tag, val first: Boolean, val last: Boolean) : TagListEntry {
        override val key: String get() = "tag-${tag.id}"
    }
}

/**
 * [shown], already in order, laid out as rows. A–Z groups them under their initial, anything not
 * starting with a letter under "#"; most used is one group with no headings.
 */
internal fun tagListEntries(shown: List<Tag>, sortByName: Boolean): List<TagListEntry> {
    fun rows(group: List<Tag>) = group.mapIndexed { i, tag ->
        TagListEntry.Row(tag, first = i == 0, last = i == group.lastIndex)
    }
    if (!sortByName) return rows(shown)
    return shown
        .groupBy { tag -> tag.name.firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() } ?: '#' }
        .flatMap { (letter, group) -> listOf(TagListEntry.Letter(letter)) + rows(group) }
}

/** How much of the bar a tag fills against the most used one. Never NaN, and never invisible when used. */
internal fun usageOf(count: Int, mostUsed: Int): Float {
    if (mostUsed <= 0 || count <= 0) return 0f
    return (count.toFloat() / mostUsed).coerceIn(0.04f, 1f)
}

private fun currentLetter(entries: List<TagListEntry>, state: LazyListState, leadingItems: Int): Char? {
    val top = (state.firstVisibleItemIndex - leadingItems).coerceIn(0, (entries.size - 1).coerceAtLeast(0))
    for (i in top downTo 0) {
        val entry = entries.getOrNull(i)
        if (entry is TagListEntry.Letter) return entry.letter
    }
    return (entries.firstOrNull() as? TagListEntry.Letter)?.letter
}

@Composable
private fun LetterHeading(letter: Char) {
    Text(
        text = letter.toString(),
        modifier = Modifier
            .padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.secondary,
    )
}

/** The letters down the right edge in A–Z. Only the ones that have tags, so none is a dead end. */
@Composable
private fun LetterIndex(
    letters: List<Char>,
    current: Char?,
    onJump: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(36.dp)
            // Clear of the New tag button, the way the rows are.
            .padding(top = 8.dp, bottom = 88.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        letters.forEach { letter ->
            val selected = letter == current
            Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 20.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                    .clickable { onJump(letter) }
                    .semantics { contentDescription = "Jump to $letter" },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TagRow(
    tag: Tag,
    first: Boolean,
    last: Boolean,
    usage: Float?,
    onOpen: (() -> Unit)?,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val colors = keyedColors(tag.name)
    // Connected, like the bookmark actions: round at the ends of the group, tight in between.
    val shape = RoundedCornerShape(
        topStart = if (first) 24.dp else 8.dp,
        topEnd = if (first) 24.dp else 8.dp,
        bottomStart = if (last) 24.dp else 8.dp,
        bottomEnd = if (last) 24.dp else 8.dp,
    )
    // A wash of the tag's own colour, so the list reads as the same colours its chips have.
    val rowColor = lerp(MaterialTheme.colorScheme.surfaceContainerLow, colors.container, 0.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(shape)
            .background(rowColor)
            .then(
                if (onOpen != null) {
                    Modifier.clickable(onClickLabel = "Show bookmarks tagged ${tag.name}", onClick = onOpen)
                } else {
                    Modifier
                }
            )
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KeyedMonogram(key = tag.name, size = 48.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (usage != null) {
                    UsageBar(fraction = usage, color = colors.content)
                }
                Text(
                    text = when (tag.nBookmarks) {
                        0 -> "No bookmarks"
                        1 -> "1 bookmark"
                        else -> "${tag.nBookmarks} bookmarks"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
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
    }
}

@Composable
private fun UsageBar(fraction: Float, color: Color) {
    Box(
        modifier = Modifier
            .width(96.dp)
            .height(6.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
private fun TagsLoadError(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Couldn't load tags",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "The server could not be reached or refused the request.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun EmptyTags(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(MaterialShapes.Clover4Leaf.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Sell,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "No tags yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
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

private val HeroHeight = 116.dp
private val DecorationSize = 230.dp
private val DecorationOverhang = 70.dp

private val PreviewTags = listOf(
    Tag(id = 1, name = "android", selected = false, nBookmarks = 214),
    Tag(id = 2, name = "design", selected = false, nBookmarks = 167),
    Tag(id = 3, name = "golang", selected = false, nBookmarks = 98),
    Tag(id = 4, name = "kotlin", selected = false, nBookmarks = 1),
    Tag(id = 5, name = "compose", selected = false, nBookmarks = 88),
    Tag(id = 6, name = "bash", selected = false, nBookmarks = 0),
)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TagsListPreview() {
    ShioriTheme {
        TagsList(tags = PreviewTags, onRename = {}, onDelete = {}, onSortChange = {}, onOpen = {})
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TagsListByNamePreview() {
    ShioriTheme {
        TagsList(tags = PreviewTags, onRename = {}, onDelete = {}, sortByName = true, onSortChange = {}, onOpen = {})
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
