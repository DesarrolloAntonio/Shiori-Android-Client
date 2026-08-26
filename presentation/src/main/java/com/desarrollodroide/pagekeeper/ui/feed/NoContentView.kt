package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.R
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

@Composable
fun NoContentView(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The illustration sits inside a tonal circle so the empty state reads as a deliberate
        // composition rather than a stray icon floating in the middle of the screen.
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
            Icon(
                modifier = Modifier
                    .padding(32.dp)
                    .size(96.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                painter = painterResource(id = R.drawable.ic_empty_list),
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = "No bookmarks yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Share a link to Shiori, or add one with the + button above.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FilledTonalButton(
            onClick = onRefresh,
            modifier = Modifier.padding(top = 24.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(text = "Refresh")
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun NoContentViewPreview() {
    ShioriTheme {
        NoContentView(onRefresh = {})
    }
}
