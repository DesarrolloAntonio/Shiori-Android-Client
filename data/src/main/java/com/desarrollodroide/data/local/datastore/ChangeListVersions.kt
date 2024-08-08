package com.desarrollodroide.data.local.datastore

/**
 * Class summarizing the local version of each model for sync
 */
data class ChangeListVersions(
    val topicVersion: Int = -1,
    val newsResourceVersion: Int = -1,
)
