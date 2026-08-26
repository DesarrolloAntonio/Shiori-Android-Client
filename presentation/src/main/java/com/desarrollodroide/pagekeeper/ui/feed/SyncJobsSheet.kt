package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.desarrollodroide.model.PendingJob
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import com.desarrollodroide.model.SyncOperationType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SyncJobsBottomSheetContent(
    pendingJobs: List<PendingJob>,
    onDismiss: () -> Unit,
    onRetryAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Pending sync jobs",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
        )
        if (pendingJobs.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                text = "Nothing waiting to sync",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column {
                    pendingJobs.forEach { job ->
                        val state = job.state.uppercase()
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            overlineContent = {
                                Text(
                                    text = job.operationType.name,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            },
                            headlineContent = {
                                Text(
                                    text = job.bookmarkTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (state == "RUNNING") {
                                        LoadingIndicator(modifier = Modifier.size(20.dp))
                                    }
                                    Text(
                                        text = job.state,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (state) {
                                            "RUNNING", "ENQUEUED" -> MaterialTheme.colorScheme.primary
                                            "BLOCKED", "FAILED" -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("Close")
            }
            Button(
                onClick = onRetryAll,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                enabled = pendingJobs.isNotEmpty(),
            ) {
                Text("Retry all")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncJobsBottomSheetContentPreview() {
    SyncJobsBottomSheetContent(
        pendingJobs = listOf(
            PendingJob(operationType = SyncOperationType.CREATE, state = "Pending", bookmarkId = 1, "Bookmark 1"),
            PendingJob(operationType = SyncOperationType.UPDATE, state = "Failed", bookmarkId = 2, "Bookmark 2"),
            PendingJob(operationType = SyncOperationType.DELETE, state = "In Progress", bookmarkId = 3, "Bookmark 3")
        ),
        onDismiss = {},
        onRetryAll = {}
    )
}
