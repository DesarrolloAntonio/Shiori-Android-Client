package com.desarrollodroide.pagekeeper.ui.components

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import com.desarrollodroide.model.Tag

/**
 * Tag is a plain data class in :model, so rememberSaveable cannot put one in a Bundle by itself.
 *
 * Only id and name are saved. `selected` and `nBookmarks` belong to whichever list the screen
 * reloads after the restore, and carrying a stale count across a configuration change would be
 * worse than recomputing it. Everything is flattened to strings because a Bundle takes a list of
 * strings but not a list of lists.
 *
 * Both savers always emit at least one element. listSaver reads an empty saved list as "there was
 * nothing to save" and hands back the initial value instead, so a user who cleared their tag
 * selection and then rotated got the cleared tags back. The leading element keeps "the user chose
 * nothing" distinguishable from "nothing was ever saved".
 */
private const val PRESENT = "1"

val TagSaver: Saver<Tag?, Any> = listSaver(
    save = { tag -> if (tag == null) listOf("") else listOf(PRESENT, tag.id.toString(), tag.name) },
    restore = { entry -> if (entry.size < 3) null else Tag(entry[1].toInt(), entry[2]) },
)

val TagListSaver: Saver<List<Tag>, Any> = listSaver(
    save = { tags -> listOf(PRESENT) + tags.flatMap { listOf(it.id.toString(), it.name) } },
    restore = { flat ->
        flat.drop(1)
            .chunked(2)
            .mapNotNull { row -> row.takeIf { it.size == 2 }?.let { Tag(it[0].toInt(), it[1]) } }
    },
)
