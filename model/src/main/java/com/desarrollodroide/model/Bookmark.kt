package com.desarrollodroide.model

data class Bookmark (
    val id: Int,
    val url: String,
    val title: String,
    val excerpt: String,
    val author: String,
    val public: Int,
    val modified: String,
    val imageURL: String,
    val hasContent: Boolean,
    val hasArchive: Boolean,
    val hasEbook: Boolean,
    val tags: List<Tag>,
    val createArchive: Boolean,
    val createEbook: Boolean,
){
    constructor(
        url: String,
        tags: List<Tag>,
        public: Int,
        createArchive: Boolean,
        createEbook: Boolean
    ) : this(
        id = -1,
        url= url,
        title = "",
        excerpt = "",
        author = "",
        public = public,
        modified = "",
        imageURL = "",
        hasContent = false,
        hasArchive = false,
        hasEbook = false,
        tags = tags,
        createArchive = createArchive,
        createEbook = createEbook,
    )

    companion object {
        fun mock() = Bookmark(
            id = -1,
            url = "url",
            title = "Bookmark title",
            excerpt = "A detailed description of the bookmark, explaining its significance, context, and why it was saved.",
            author = "John Doe",
            public = 1,
            modified = "2024-03-19 15:44:40",
            imageURL = "https://fastly.picsum.photos/id/12/2500/1667.jpg?hmac=Pe3284luVre9ZqNzv1jMFpLihFI6lwq7TPgMSsNXw2w",
            hasContent = true,
            hasArchive = true,
            hasEbook = false,
            createArchive = true,
            createEbook = true,
            tags = listOf(Tag("tag1"), Tag("tag2")),
        )
    }
}
