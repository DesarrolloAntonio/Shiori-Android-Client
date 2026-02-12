package com.desarrollodroide.network.model

data class ReadableContentResponseDTO (
    val ok: Boolean?,
    val message: ReadableMessageDto?,
    // v1.8.0 returns content and html at root level (no wrapper)
    val content: String? = null,
    val html: String? = null,
) {
    fun resolvedMessage(): ReadableMessageDto? =
        message ?: if (content != null || html != null) ReadableMessageDto(content, html) else null
}

