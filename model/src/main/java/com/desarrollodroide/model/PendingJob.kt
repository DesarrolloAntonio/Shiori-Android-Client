package com.desarrollodroide.model

data class PendingJob(
    val operationType: SyncOperationType,
    val state: String,
    val bookmarkId: Int,
    val bookmarkTitle: String
)

enum class SyncOperationType {
    CREATE, UPDATE, DELETE;

    companion object {
        fun fromString(value: String): SyncOperationType? =
            entries.find { it.name == value.uppercase() }
    }
}
