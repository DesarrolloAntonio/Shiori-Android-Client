package com.desarrollodroide.data.mapper

import com.desarrollodroide.data.UserPreferences
import com.desarrollodroide.data.helpers.AddTagDTOAdapter
import com.desarrollodroide.data.helpers.TagTypeAdapter
import com.desarrollodroide.data.local.room.entity.BookmarkEntity
import com.desarrollodroide.data.local.room.entity.TagEntity
import com.desarrollodroide.model.*
import com.desarrollodroide.network.model.*
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder

fun SessionDTO.toDomainModel() = User(
    token = token?:"",
    session = session?:"",
    account = account?.toDomainModel()?:Account()
)

fun AccountDTO.toDomainModel() = Account(
    id = -1,
    userName = userName?:"",
    password = password?:"",
    owner = isOwner?:false,
    serverUrl = "",
)

fun SessionDTO.toProtoEntity(): UserPreferences = UserPreferences.newBuilder()
    .setSession(session?:"")
    .setUsername(account?.userName?:"")
    .setId(account?.id?:-1)
    .setOwner(account?.isOwner?:false)
    .build()

fun BookmarkDTO.toDomainModel(serverUrl: String = "") = Bookmark(
    id = id?:0,
    url = url?:"",
    title = title?:"",
    excerpt = excerpt?:"",
    author = author?:"",
    public = public?:0,
    createAt = createdAt?:"",
    modified = modified?:"",
    imageURL = "$serverUrl$imageURL",
    hasContent = hasContent?:false,
    hasArchive = hasArchive?:false,
    hasEbook = hasEbook?:false,
    tags = tags?.map { it.toDomainModel() }?: emptyList(),
    createArchive = createArchive?:false,
    createEbook = createEbook?:false,
)

fun BookmarksDTO.toDomainModel(serverUrl: String) = Bookmarks(
    error = "",
    page = page?:0,
    maxPage = maxPage?:0,
    bookmarks = bookmarks?.map { it.toDomainModel(serverUrl) }?: emptyList()
)

fun TagDTO.toDomainModel() = Tag(
    id = id?:0,
    name = name?:"",
    selected = false,
    nBookmarks = nBookmarks?:0
)

fun TagDTO.toEntityModel() = TagEntity(
    id = id?:0,
    name = name?:"",
    nBookmarks = nBookmarks?:0
)

fun TagEntity.toDomainModel() = Tag(
    id = id,
    name = name,
    selected = false,
    nBookmarks = nBookmarks
)

fun Account.toRequestBody() =
    LoginRequestPayload(
        username = userName,
        password = password
    )

fun Tag.toEntityModel() = TagEntity(
    id = id,
    name = name,
    nBookmarks = nBookmarks
)

fun BookmarkDTO.toEntityModel() = BookmarkEntity(
    id = id?:0,
    url = url?:"",
    title = title?:"",
    excerpt = excerpt?:"",
    author = author?:"",
    isPublic = public?:0,
    createdAt = createdAt?:"",
    modified = modified?:"",
    imageURL = imageURL?:"",
    hasContent = hasContent?:false,
    hasArchive = hasArchive?:false,
    hasEbook = hasEbook?:false,
    tags = tags?.map { it.toDomainModel() } ?: emptyList(),
    createArchive = createArchive?:false,
    createEbook = createEbook?:false,
)

fun BookmarkEntity.toDomainModel() = Bookmark(
    id = id,
    url = url,
    title = title,
    excerpt = excerpt,
    author = author,
    public = isPublic,
    createAt = createdAt,
    modified = modified,
    imageURL = imageURL,
    hasContent = hasContent,
    hasArchive = hasArchive,
    hasEbook = hasEbook,
    tags = tags,
    createArchive = createArchive,
    createEbook = createEbook,
)

fun Bookmark.toEntityModel(modified: String? = null) = BookmarkEntity(
    id = id,
    url = url,
    title = title,
    excerpt = excerpt,
    author = author,
    isPublic = public,
    createdAt = createAt,
    modified = modified ?: this.modified,
    imageURL = imageURL,
    hasContent = hasContent,
    hasArchive = hasArchive,
    hasEbook = hasEbook,
    tags = tags,
    createArchive = createArchive,
    createEbook = createEbook,
)

fun UpdateCachePayload.toDTO() = UpdateCachePayloadDTO(
    createArchive = createArchive,
    createEbook = createEbook,
    ids = ids,
    keepMetadata = keepMetadata,
)

fun UpdateCachePayload.toV1DTO() = UpdateCachePayloadV1DTO(
    createArchive = createArchive,
    createEbook = createEbook,
    ids = ids,
    keepMetadata = keepMetadata,
    skipExist = skipExist
)

fun LivenessResponseDTO.toDomainModel() = LivenessResponse(
    ok = ok?:false,
    message = message?.toDomainModel()
)

fun ReleaseInfoDTO.toDomainModel() = ReleaseInfo(
    version = version?:"",
    date = date?:"",
    commit = commit?:""
)

fun LoginResponseDTO.toProtoEntity(
    userName: String,
): UserPreferences = UserPreferences.newBuilder()
    .setSession(message?.session ?: message?.token ?: "")
    .setUsername(userName)
    .setToken(message?.token?:"")
    .build()

fun LoginResponseMessageDTO.toDomainModel() = LoginResponseMessage(
    expires = expires?:0,
    session = session?:"",
    token = token?:""
)

fun ReadableContentResponseDTO.toDomainModel() = ReadableContent(
    ok = ok?:false,
    message = message?.toDomainModel() ?: ReadableMessage("", "")
)

fun ReadableMessageDto.toDomainModel() = ReadableMessage(
    content = content?:"",
    html = html?:""
)


fun SyncBookmarksResponseDTO.toDomainModel(): SyncBookmarksResponse {
    return SyncBookmarksResponse(
        deleted = message.deleted ?: emptyList(),
        modified = message.modified?.toDomainModel() ?: ModifiedBookmarks(emptyList(), 0, 0)
    )
}

fun ModifiedBookmarksDTO.toDomainModel(): ModifiedBookmarks {
    return ModifiedBookmarks(
        bookmarks = bookmarks?.map { it.toDomainModel() } ?: emptyList(),
        maxPage = maxPage ?: 0,
        page = page ?: 0
    )
}

fun Bookmark.toAddBookmarkDTO() = BookmarkDTO(
    id = null,
    url = url,
    title = title,
    excerpt = excerpt,
    author = null,
    public = public,
    createdAt = null,
    modified = null,
    imageURL = null,
    hasContent = null,
    hasArchive = null,
    hasEbook = null,
    tags = tags.map { TagDTO(id = null, name = it.name, nBookmarks = null) },
    createArchive = createArchive,
    createEbook = createEbook
)

fun Bookmark.toEditBookmarkDTO() = BookmarkDTO(
    id = id,
    url = url,
    title = title,
    excerpt = excerpt,
    author = author,
    public = public,
    createdAt = createAt,
    modified = modified,
    imageURL = imageURL,
    hasContent = hasContent,
    hasArchive = hasArchive,
    hasEbook = hasEbook,
    tags = tags.map { TagDTO(id = it.id, name = it.name, nBookmarks = null) },
    createArchive = createArchive,
    createEbook = createEbook
)

/**
 * Converts a Bookmark to JSON format for updating existing bookmarks.
 * Includes all fields of the bookmark in the JSON output.
 */
fun BookmarkDTO.toEditBookmarkJson() = GsonBuilder()
    .registerTypeAdapter(TagDTO::class.java, AddTagDTOAdapter())
    .setExclusionStrategies(object : ExclusionStrategy {
        override fun shouldSkipField(f: FieldAttributes): Boolean {
            return f.name == "hasEbook" || f.name == "createEbook"
        }
        override fun shouldSkipClass(clazz: Class<*>): Boolean = false
    })
    .create()
    .toJson(this)



