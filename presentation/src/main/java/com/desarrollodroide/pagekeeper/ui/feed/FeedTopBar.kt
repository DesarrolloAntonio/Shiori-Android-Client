package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.model.Tag
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme
import com.desarrollodroide.pagekeeper.ui.theme.keyedColors
import java.text.NumberFormat

/**
 * The library's header: how many bookmarks there are, in large type, the search field with the
 * tag filter inside it, and the tags currently applied.
 *
 * The count and its decoration collapse completely as the list scrolls, leaving the search row
 * pinned, so reading the list costs no more chrome than the old single-row bar did. On a short
 * window they are not drawn at all.
 *
 * Settings and sync are not here: they live in the floating toolbar at the bottom, and what is
 * waiting to sync has its own banner at the top of the list.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    selectedTags: List<Tag>,
    onRemoveTag: (Tag) -> Unit,
    onClearTags: () -> Unit,
    showOnlyHiddenTag: Boolean,
    bookmarkCount: Int? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showTitle: Boolean = true,
) {
    val collapsedFraction = scrollBehavior?.state?.collapsedFraction ?: 0f
    val decoration = MaterialShapes.Flower.toShape()
    val decorationColor = MaterialTheme.colorScheme.tertiaryContainer

    Surface(color = MaterialTheme.colorScheme.surface) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                // A large shape bleeding off the top corner, behind the count. Drawn rather than
                // laid out, so it takes no space, and it fades as the count collapses.
                .drawBehind {
                    if (!showTitle) return@drawBehind
                    val side = DecorationSize.toPx()
                    val outline = decoration.createOutline(Size(side, side), layoutDirection, this)
                    translate(
                        left = size.width - side + DecorationOverhang.toPx(),
                        top = -side / 2f,
                    ) {
                        drawOutline(outline, decorationColor, alpha = 1f - collapsedFraction)
                    }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
            ) {
                if (showTitle) {
                    CollapsingLibraryHero(
                        bookmarkCount = bookmarkCount,
                        narrowing = libraryNarrowing(
                            tagCount = if (showOnlyHiddenTag) 0 else selectedTags.size,
                            searching = searchQuery.isNotBlank(),
                            showOnlyHiddenTag = showOnlyHiddenTag,
                        ),
                        scrollBehavior = scrollBehavior,
                    )
                }
                SearchField(
                    searchQuery = searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    onClearSearch = onClearSearch,
                    onFilterClick = onFilterClick,
                    selectedCount = selectedTags.size,
                    showOnlyHiddenTag = showOnlyHiddenTag,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                // Only what is applied, never suggestions: with two hundred tags any pick of
                // "popular" ones is close to arbitrary, and a row that changes by itself is harder
                // to trust.
                AnimatedVisibility(
                    visible = selectedTags.isNotEmpty() && !showOnlyHiddenTag,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    AppliedTagFilters(
                        tags = selectedTags,
                        onRemoveTag = onRemoveTag,
                        onClearTags = onClearTags,
                    )
                }
            }
        }
    }
}

/** What the line above the count says, and the note beside it, for what is narrowing the list. */
internal data class LibraryNarrowing(val overline: String, val note: String?)

/**
 * The count is of what the list shows, so the words around it say why it may be smaller than the
 * library: a tag filter, a search, both, or the hidden tag view.
 */
internal fun libraryNarrowing(tagCount: Int, searching: Boolean, showOnlyHiddenTag: Boolean): LibraryNarrowing {
    val tags = when (tagCount) {
        0 -> null
        1 -> "1 tag"
        else -> "$tagCount tags"
    }
    return when {
        showOnlyHiddenTag -> LibraryNarrowing(
            overline = "HIDDEN TAG",
            note = if (searching) "matching your search" else null,
        )
        tags != null && searching -> LibraryNarrowing("FILTERED", "$tags · matching your search")
        tags != null -> LibraryNarrowing("FILTERED", "showing $tags")
        searching -> LibraryNarrowing("SEARCH", "matching your search")
        else -> LibraryNarrowing("YOUR LIBRARY", null)
    }
}

/**
 * "YOUR LIBRARY" and the count, set large. It takes its height from the scroll behaviour, so it
 * shrinks to nothing as the list moves up and comes back when the list returns to the top.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingLibraryHero(
    bookmarkCount: Int?,
    narrowing: LibraryNarrowing,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { HeroHeight.toPx() }
    val state = scrollBehavior?.state
    if (state != null) {
        SideEffect {
            if (state.heightOffsetLimit != -expandedHeightPx) {
                state.heightOffsetLimit = -expandedHeightPx
            }
        }
    }
    val offsetPx = state?.heightOffset ?: 0f
    val visibleHeight: Dp = with(density) { (expandedHeightPx + offsetPx).coerceAtLeast(0f).toDp() }

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
                .padding(start = 20.dp, end = 20.dp, top = 8.dp)
                .graphicsLayer { alpha = 1f - (state?.collapsedFraction ?: 0f) },
        ) {
            Text(
                text = narrowing.overline,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            if (bookmarkCount == null) {
                // Room has not answered yet. A word rather than a zero, so a full library never
                // flashes as empty.
                Text(
                    text = "Library",
                    style = heroNumberStyle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = NumberFormat.getIntegerInstance().format(bookmarkCount),
                        style = heroNumberStyle(),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = if (bookmarkCount == 1) "bookmark" else "bookmarks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (narrowing.note != null) {
                            Text(
                                text = narrowing.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * A real field, not a button that opens a search screen: typing filters the list underneath, which
 * is what the web does. The tag filter sits inside it, at the end, because it narrows the same
 * list the search does.
 */
@Composable
private fun SearchField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onFilterClick: () -> Unit,
    selectedCount: Int,
    showOnlyHiddenTag: Boolean,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
                    .semantics { contentDescription = "Search bookmarks" },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                // The results are already on screen, so Search only has to put the keyboard away.
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
            AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onClearSearch()
                        keyboardController?.hide()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TagFilterButton(
                onClick = onFilterClick,
                selectedCount = selectedCount,
                showOnlyHiddenTag = showOnlyHiddenTag,
            )
        }
    }
}

/**
 * Opens the tag filter. Round and tonal while nothing is filtered; filled, squarer and counting the
 * tags once something is, so a filtered list never passes for the whole library.
 */
@Composable
private fun TagFilterButton(
    onClick: () -> Unit,
    selectedCount: Int,
    showOnlyHiddenTag: Boolean,
) {
    val active = selectedCount > 0 || showOnlyHiddenTag
    val corner by animateDpAsState(if (active) 12.dp else 20.dp, label = "filterCorner")
    BadgedBox(
        badge = {
            AnimatedVisibility(
                visible = selectedCount > 0 && !showOnlyHiddenTag,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ) { Text(selectedCount.toString()) }
            }
        },
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .semantics {
                    contentDescription = if (showOnlyHiddenTag) "Hidden tags" else "Filter by tag"
                },
            shape = RoundedCornerShape(corner),
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (showOnlyHiddenTag) Icons.Default.VisibilityOff else Icons.Filled.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** The tags the list is filtered by, each removable and in its own colour, plus Clear. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppliedTagFilters(
    tags: List<Tag>,
    onRemoveTag: (Tag) -> Unit,
    onClearTags: () -> Unit,
) {
    // Wraps rather than scrolls sideways: these are only the tags the user picked, so there are
    // few, and every one of them should stay visible.
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        tags.forEach { tag ->
            val colors = keyedColors(tag.name)
            InputChip(
                selected = true,
                onClick = { onRemoveTag(tag) },
                label = { Text(tag.name, fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Remove ${tag.name} filter",
                        modifier = Modifier.size(InputChipDefaults.IconSize),
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = InputChipDefaults.inputChipColors(
                    selectedContainerColor = colors.container,
                    selectedLabelColor = colors.content,
                    selectedTrailingIconColor = colors.content,
                ),
                border = null,
            )
        }
        if (tags.size > 1) {
            TextButton(onClick = onClearTags) { Text("Clear", fontWeight = FontWeight.Bold) }
        }
    }
}

/**
 * The header while bookmarks are picked: how many, set as large as the library's own count, and a
 * way out. The actions moved to a floating bar at the bottom, where the library's tools were, so
 * they sit under the thumb that is doing the picking.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(bottom = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onClose,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel selection")
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // The number rolls as bookmarks are picked and dropped, so the change is seen
                // without reading it.
                AnimatedContent(
                    targetState = selectedCount,
                    transitionSpec = {
                        val up = targetState > initialState
                        (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                            (slideOutVertically { if (up) -it else it } + fadeOut())
                    },
                    label = "selected count",
                ) { count ->
                    Text(
                        text = count.toString(),
                        style = heroNumberStyle(),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
                Text(
                    text = "selected",
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/** Icon button that carries a count badge, animated in and out. */
@Composable
internal fun BadgedIconButton(
    onClick: () -> Unit,
    badgeCount: Int,
    description: String,
    icon: @Composable () -> Unit,
) {
    BadgedBox(
        badge = {
            AnimatedVisibility(
                visible = badgeCount > 0,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    ) {
        // The description goes on the button, not the glyph, so TalkBack announces the action once
        // rather than describing the icon inside a separately labelled clickable.
        IconButton(
            onClick = onClick,
            modifier = Modifier.semantics { this.contentDescription = description },
        ) {
            icon()
        }
    }
}

/**
 * The header's scroll connection, for the list to attach to itself.
 *
 * It has to be the list's nearest nested scroll parent. Further out, pull to refresh sees a pull
 * at the top of the list first and takes all of it, so the collapsed header could never expand.
 */
internal val LocalLibraryScrollConnection = staticCompositionLocalOf<NestedScrollConnection?> { null }

/** The overline and the count: top padding, one label line and one 76sp line. */
private val HeroHeight = 108.dp

/** The decorative shape behind the count, and how far it hangs off the right edge. */
private val DecorationSize = 300.dp
private val DecorationOverhang = 120.dp

@Preview(showBackground = true)
@Composable
private fun TopBarPreview() {
    ShioriTheme {
        TopBar(
            searchQuery = "",
            onSearchQueryChange = { },
            onClearSearch = { },
            onFilterClick = { },
            selectedTags = emptyList(),
            onRemoveTag = { },
            onClearTags = { },
            showOnlyHiddenTag = false,
            bookmarkCount = 1248,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TopBarFilteredPreview() {
    ShioriTheme {
        TopBar(
            searchQuery = "kotlin",
            onSearchQueryChange = { },
            onClearSearch = { },
            onFilterClick = { },
            selectedTags = listOf(
                Tag(id = 1, name = "android", selected = true, nBookmarks = 0),
                Tag(id = 2, name = "design", selected = true, nBookmarks = 0),
            ),
            onRemoveTag = { },
            onClearTags = { },
            showOnlyHiddenTag = false,
            bookmarkCount = 1248,
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
        )
    }
}
