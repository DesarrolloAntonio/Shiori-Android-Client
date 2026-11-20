package com.desarrollodroide.pagekeeper.ui.feed.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.desarrollodroide.data.extensions.isTimestampId
import com.desarrollodroide.pagekeeper.R

/**
 * A bookmark's actions, as one connected group: the ends are rounded, the joins between buttons
 * are tight, so the row reads as a single control rather than loose icons.
 *
 * Built from plain buttons, not the Expressive ButtonGroup. ButtonGroup crashes when measured inside
 * a lazy list: ButtonGroupMeasurePolicy builds constraints with maxWidth below minWidth and throws,
 * taking the whole feed down on the first frame. See BookmarkItemLayoutTest, which fails against
 * ButtonGroup and passes against this.
 */
@Composable
fun ButtonsView(
    getBookmark: GetBookmark,
    actions: BookmarkActions,
    modifier: Modifier = Modifier,
) {
    val bookmark by remember { derivedStateOf(getBookmark) }

    val items = buildList {
        add(CardAction("Edit", rememberVectorPainter(Icons.Outlined.Edit)) { actions.onClickEdit(getBookmark) })
        add(CardAction("Share", rememberVectorPainter(Icons.Outlined.Share)) { actions.onClickShare(getBookmark) })
        if (bookmark.hasEbook) {
            add(CardAction("Epub", painterResource(id = R.drawable.ic_book)) { actions.onClickEpub(getBookmark) })
        }
        if (!bookmark.id.isTimestampId()) {
            add(CardAction("Update", rememberVectorPainter(Icons.Outlined.CloudUpload)) { actions.onClickSync(getBookmark) })
        }
        add(
            CardAction(
                description = "Delete",
                icon = rememberVectorPainter(Icons.Outlined.Delete),
                tint = MaterialTheme.colorScheme.error,
            ) { actions.onClickDelete(getBookmark) }
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            FilledIconButton(
                onClick = item.onClick,
                // 48dp tall, so the button is its own touch target and the minimum interactive
                // size does not pad gaps between the joined shapes.
                modifier = Modifier.size(width = 52.dp, height = 48.dp),
                shape = connectedShape(index, items.size),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = item.tint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(painter = item.icon, contentDescription = item.description)
            }
        }
    }
}

private class CardAction(
    val description: String,
    val icon: Painter,
    val tint: Color? = null,
    val onClick: () -> Unit,
)

/** Rounded outer ends, tight inner joins. One button on its own is simply round. */
private fun connectedShape(index: Int, count: Int): Shape {
    val outer = 24.dp
    val inner = 8.dp
    return when {
        count == 1 -> CircleShape
        index == 0 -> RoundedCornerShape(topStart = outer, bottomStart = outer, topEnd = inner, bottomEnd = inner)
        index == count - 1 -> RoundedCornerShape(topStart = inner, bottomStart = inner, topEnd = outer, bottomEnd = outer)
        else -> RoundedCornerShape(inner)
    }
}
