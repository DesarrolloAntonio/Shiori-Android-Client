package com.desarrollodroide.network.model

data class BookmarkResponseDTO (
    val ok: Boolean?,
    val message: List<BookmarkDTO>?,
)

/**
 * Wrapper for single bookmark responses from Shiori v1.8.0+.
 * The MessageResponseMiddleware wraps all responses in {"ok":bool,"message":data}.
 * For addBookmark/editBookmark, the legacy handler returns a single BookmarkDTO,
 * which gets wrapped as {"ok":true,"message":{id:...,url:...,...}}.
 *
 * Also handles legacy format (pre-middleware) where the BookmarkDTO is the root object.
 */
data class SingleBookmarkResponseDTO(
    val ok: Boolean? = null,
    val message: BookmarkDTO? = null,
    // Legacy fallback fields (when response is not wrapped)
    val id: Int? = null,
    val url: String? = null,
    val title: String? = null,
    val excerpt: String? = null,
    val author: String? = null,
    val public: Int? = null,
    val createdAt: String? = null,
    val modified: String? = null,
    val imageURL: String? = null,
    val hasContent: Boolean? = null,
    val hasArchive: Boolean? = null,
    val hasEbook: Boolean? = null,
    val tags: List<TagDTO>? = null,
) {
    fun resolvedBookmark(): BookmarkDTO? =
        message ?: if (id != null) BookmarkDTO(
            id = id, url = url, title = title, excerpt = excerpt,
            author = author, public = public, createdAt = createdAt,
            modified = modified, imageURL = imageURL, hasContent = hasContent,
            hasArchive = hasArchive, hasEbook = hasEbook, tags = tags,
            createArchive = null, createEbook = null
        ) else null
}