package com.desarrollodroide.pagekeeper.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.model.PendingJob
import com.desarrollodroide.model.SyncOperationType
import com.desarrollodroide.pagekeeper.ui.theme.LocalExtendedColors
import com.desarrollodroide.pagekeeper.ui.theme.ShioriTheme

/**
 * What is waiting to reach the server: one row per job, coloured by what it does, with where it
 * is. Scrolls, because an afternoon offline can queue more than a sheet's height of them.
 */
@Composable
fun SyncJobsBottomSheetContent(
    pendingJobs: List<PendingJob>,
    onDismiss: () -> Unit,
    onRetryAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Pending sync jobs",
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-1).sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 16.dp),
        )
        if (pendingJobs.isEmpty()) {
            AllSynced()
        } else {
            // Connected rows: round at the ends of the list, tight in between.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                pendingJobs.forEach { job -> SyncJobRow(job) }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss, modifier = Modifier.height(48.dp), shape = CircleShape) {
                Text("Close", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = onRetryAll,
                modifier = Modifier.height(56.dp),
                shape = CircleShape,
                contentPadding = PaddingValues(start = 20.dp, end = 24.dp),
                enabled = pendingJobs.isNotEmpty(),
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry all", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SyncJobRow(job: PendingJob) {
    val scheme = MaterialTheme.colorScheme
    val (icon, container, content) = operationLook(job.operationType)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(scheme.surfaceContainerLowest)
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(container),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = job.operationType.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = scheme.onSurfaceVariant,
            )
            Text(
                // Jobs queued before titles fell back to the url can still arrive blank.
                text = job.bookmarkTitle.ifBlank { "Untitled bookmark" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        JobState(job.state)
    }
}

private data class OperationLook(val icon: ImageVector, val container: Color, val content: Color)

/** Each kind of change gets its own colour, so a queue of deletes reads differently from saves. */
@Composable
private fun operationLook(type: SyncOperationType): OperationLook {
    val scheme = MaterialTheme.colorScheme
    val extended = LocalExtendedColors.current
    return when (type) {
        SyncOperationType.CREATE -> OperationLook(Icons.Outlined.BookmarkAdd, scheme.secondaryContainer, scheme.onSecondaryContainer)
        SyncOperationType.UPDATE -> OperationLook(Icons.Outlined.Edit, scheme.primaryContainer, scheme.onPrimaryContainer)
        SyncOperationType.DELETE -> OperationLook(Icons.Outlined.Delete, scheme.errorContainer, scheme.onErrorContainer)
        SyncOperationType.CACHE -> OperationLook(Icons.Outlined.CloudDownload, extended.goldContainer, extended.onGoldContainer)
    }
}

/**
 * Where a job is, in words rather than WorkManager's names. BLOCKED used to be shown in the error
 * colour, but it only means the job is waiting for the one before it.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun JobState(state: String) {
    val scheme = MaterialTheme.colorScheme
    when (state.uppercase()) {
        "RUNNING" -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LoadingIndicator(modifier = Modifier.size(24.dp), color = scheme.primary)
            Text("In progress", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = scheme.primary)
        }
        "FAILED" -> StateChip(
            text = "Failed",
            container = scheme.errorContainer,
            content = scheme.onErrorContainer,
            icon = Icons.Outlined.ErrorOutline,
        )
        "ENQUEUED" -> StateChip(text = "Pending", container = scheme.surfaceContainerHighest, content = scheme.onSurface)
        "BLOCKED" -> StateChip(text = "Waiting", container = scheme.surfaceContainerHighest, content = scheme.onSurface)
        "CANCELLED" -> StateChip(text = "Cancelled", container = scheme.surfaceContainerHighest, content = scheme.onSurfaceVariant)
        else -> StateChip(
            text = state.lowercase().replaceFirstChar { it.uppercase() },
            container = scheme.surfaceContainerHighest,
            content = scheme.onSurface,
        )
    }
}

@Composable
private fun StateChip(text: String, container: Color, content: Color, icon: ImageVector? = null) {
    Surface(shape = CircleShape, color = container, contentColor = content) {
        Row(
            modifier = Modifier
                .height(28.dp)
                .padding(start = if (icon != null) 8.dp else 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** The queue is empty: said plainly, with the cloud ticked, so an empty sheet does not look broken. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AllSynced() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(
            text = "All synced",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Nothing waiting to sync",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncJobsBottomSheetContentPreview() {
    ShioriTheme {
        SyncJobsBottomSheetContent(
            pendingJobs = listOf(
                PendingJob(operationType = SyncOperationType.CREATE, state = "RUNNING", bookmarkId = 1, "github.com/go-shiori/shiori"),
                PendingJob(operationType = SyncOperationType.UPDATE, state = "FAILED", bookmarkId = 2, "How SQLite Is Tested"),
                PendingJob(operationType = SyncOperationType.DELETE, state = "ENQUEUED", bookmarkId = 3, "Effective Go")
            ),
            onDismiss = {},
            onRetryAll = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncJobsEmptyPreview() {
    ShioriTheme {
        SyncJobsBottomSheetContent(pendingJobs = emptyList(), onDismiss = {}, onRetryAll = {})
    }
}
