package com.desarrollodroide.data.repository

interface SyncManager {

    fun scheduleSyncWork(operationType: SyncOperationType, bookmarkId: Int)
}