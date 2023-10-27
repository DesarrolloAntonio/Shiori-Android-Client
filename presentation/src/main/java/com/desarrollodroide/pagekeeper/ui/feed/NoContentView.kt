package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.pagekeeper.R

@Composable
fun NoContentView(
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            modifier = Modifier.width(100.dp),
            tint = MaterialTheme.colorScheme.secondary,
            painter = painterResource(id = R.drawable.ic_empty_list),
            contentDescription = "No content image"
        )
        Button(
            onClick = onRefresh,
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = "Refresh")
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 320,
    heightDp = 480
)
@Composable
fun NoContentViewPreview() {
    NoContentView(onRefresh = {})
}