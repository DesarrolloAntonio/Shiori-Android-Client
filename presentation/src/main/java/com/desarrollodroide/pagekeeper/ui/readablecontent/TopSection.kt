package com.desarrollodroide.pagekeeper.ui.readablecontent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.pagekeeper.extensions.asLocalBookmarkDate
import com.desarrollodroide.pagekeeper.ui.feed.item.BookmarkThumbnailPlaceholder
import com.desarrollodroide.pagekeeper.ui.feed.item.hostOf
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * The block above an article: where it is from, when it was saved, and what it is called, set
 * large, with a way to the original.
 *
 * It used to be a centred date, a centred 24sp title and a View Original button on a row of its
 * own. The title now leads, in the display weight the library uses, and the site and date sit above
 * it with the site's monogram, so an article is recognisable the same way its card is.
 *
 * [onClose] turns it into the header of the two pane detail, which has no app bar: the close button
 * moves in here, on the same row as the link to the original.
 *
 * [date] is the bookmark's time as the server sends it (UTC). It is shown as the cards show it, a
 * date in the device's zone.
 */
@Composable
fun TopSection(
    title: String,
    date: String,
    onClick: () -> Unit,
    onClose: (() -> Unit)? = null,
    url: String = "",
) {
    val shownDate = date.asLocalBookmarkDate()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (onClose != null) 8.dp else 24.dp, end = 16.dp),
    ) {
        if (onClose != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close article")
                }
                Spacer(modifier = Modifier.weight(1f))
                OriginalButton(onClick = onClick)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        Column(modifier = Modifier.padding(start = if (onClose != null) 16.dp else 0.dp, end = 8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (url.isNotEmpty()) {
                    BookmarkThumbnailPlaceholder(url = url, size = 40.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (url.isNotEmpty()) {
                        Text(
                            text = hostOf(url),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        text = shownDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onClose == null) {
                    OriginalButton(onClick = onClick)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = if (onClose != null) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OriginalButton(onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text("Original")
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Preview(showBackground = true)
@Composable
private fun TopSectionPreview() {
    ShioriTheme {
        TopSection(
            title = "A Developer’s Roadmap to Predictive Back (Views)",
            date = "2024-05-27 16:41:09",
            url = "https://android-developers.googleblog.com/2024/05/predictive-back.html",
            onClick = {}
        )
    }
}
