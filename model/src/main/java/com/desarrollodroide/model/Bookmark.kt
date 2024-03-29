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
}
