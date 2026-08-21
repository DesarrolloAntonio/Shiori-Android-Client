package com.desarrollodroide.network.model

/**
 * Response shape for the single-tag v1 endpoints. Like every other v1 response it arrives wrapped
 * by the server's message middleware as `{"ok": bool, "message": {...}}`.
 */
data class SingleTagDTO(
    val ok: Boolean?,
    val message: TagDTO?,
)
