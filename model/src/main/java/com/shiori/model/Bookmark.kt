package com.shiori.model

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
    val tags: List<Tag>,
    val createArchive: Boolean
){
    constructor(
        url: String,
        tags: List<Tag>
    ) : this(
        id = -1,
        url= url,
        title = "",
        excerpt = "",
        author = "",
        public = 1,
        modified = "",
        imageURL = "",
        hasContent = false,
        hasArchive = false,
        tags = tags,
        createArchive = false
    )
}
