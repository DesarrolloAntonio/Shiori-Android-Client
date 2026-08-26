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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The block above an article: when it was saved, what it is called, and a way to the original.
 *
 * [onClose] turns it into the header of the two pane detail. The pane cannot afford a 64dp app bar
 * holding nothing but a back arrow, so the arrow moves in here, on the same row as View Original,
 * which was a row of its own with empty space either side of it. Everything goes left aligned:
 * centred headings read as a poster on a full screen and as an accident in a narrow column.
 */
@Composable
fun TopSection(
    title: String,
    date: String,
    onClick: () -> Unit,
    onClose: (() -> Unit)? = null,
) {
    if (onClose != null) {
        PaneTopSection(title = title, date = date, onClick = onClick, onClose = onClose)
        return
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = date,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onClick,
            ) {
                Text("View Original", color = Color.White)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
    }
}

@Composable
private fun PaneTopSection(
    title: String,
    date: String,
    onClick: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close article")
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onClick) {
                Text("View Original")
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.N)
@Preview(showBackground = true)
@Composable
fun TopSectionPreview() {
    MaterialTheme {
        TopSection(
            title = "A Developer’s Roadmap to Predictive Back (Views)",
            date = "Added 27 May 2024, 16:41:09",
            onClick = {}
        )
    }
}

